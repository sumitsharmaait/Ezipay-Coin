package com.app.ezipaycoin.presentation.kyc

import android.os.Build
import com.app.ezipaycoin.utils.UserMessages
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.LivenessClientMessage
import com.app.ezipaycoin.data.remote.dto.LivenessServerMessage
import com.app.ezipaycoin.data.remote.dto.request.KycDocumentType
import com.app.ezipaycoin.data.remote.dto.request.KycSubmitRequest
import com.app.ezipaycoin.data.remote.liveness.LivenessSocketClient
import com.app.ezipaycoin.data.remote.liveness.LivenessSocketEvent
import com.app.ezipaycoin.domain.repository.KycRepository
import com.app.ezipaycoin.domain.repository.KycVideoRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KycViewModel(
    private val repository: KycRepository,
    private val videoRepository: KycVideoRepository,
    private val socketClient: LivenessSocketClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(KycState())
    val uiState: StateFlow<KycState> = _uiState.asStateFlow()

    private var livenessJob: Job? = null

    init {
        fetchStatus()
    }

    fun onEvent(event: KycEvent) {
        when (event) {
            is KycEvent.FullNameChanged -> _uiState.update { it.copy(fullName = event.value) }
            is KycEvent.DateOfBirthChanged -> _uiState.update { it.copy(dateOfBirth = event.value) }
            is KycEvent.NationalityChanged -> _uiState.update { it.copy(nationality = event.value) }

            is KycEvent.AddressLineChanged -> _uiState.update { it.copy(addressLine = event.value) }
            is KycEvent.CityChanged -> _uiState.update { it.copy(city = event.value) }
            is KycEvent.PostalCodeChanged -> _uiState.update { it.copy(postalCode = event.value) }
            is KycEvent.CountryChanged -> _uiState.update { it.copy(country = event.value) }
            is KycEvent.ProofOfAddressPicked -> _uiState.update { it.copy(proofOfAddressPath = event.path) }

            is KycEvent.DocumentTypeSelected -> _uiState.update {
                val newSpec = KycDocumentCatalog.byId(event.id)
                it.copy(
                    selectedDocTypeId = event.id,
                    documentNumber = "",
                    backImagePath = if (newSpec.requiresBack) it.backImagePath else ""
                )
            }

            is KycEvent.DocumentNumberChanged -> _uiState.update { it.copy(documentNumber = event.value) }
            is KycEvent.FrontImagePicked -> _uiState.update { it.copy(frontImagePath = event.path) }
            is KycEvent.BackImagePicked -> _uiState.update { it.copy(backImagePath = event.path) }
            is KycEvent.SelfieImagePicked -> _uiState.update { it.copy(selfieImagePath = event.path) }

            is KycEvent.LivenessToggled -> _uiState.update {
                it.copy(
                    livenessEnabled = event.enabled,
                    livenessVerified = if (event.enabled) it.livenessVerified else false,
                    videoPath = if (event.enabled) "" else it.videoPath,
                    liveness = if (event.enabled) it.liveness else LivenessUiState()
                )
            }

            is KycEvent.StartLiveness -> startLiveness()
            is KycEvent.StopLiveness -> stopLiveness()
            is KycEvent.LivenessFrame -> socketClient.send(
                LivenessClientMessage(type = "FRAME", frameBase64 = event.base64)
            )

            is KycEvent.VideoRecorded -> uploadRecordedVideo(event.path)

            is KycEvent.ConsentToggled -> _uiState.update { it.copy(consentAccepted = event.accepted) }

            is KycEvent.Submit -> submit()
            is KycEvent.Retry -> {
                _uiState.update { it.copy(statusResponse = ResponseState.Idle) }
                fetchStatus()
            }

            is KycEvent.DismissDialog -> _uiState.update {
                when (event.which) {
                    0 -> it.copy(statusResponse = ResponseState.Idle)
                    1 -> it.copy(submitResponse = ResponseState.Idle)
                    else -> it
                }
            }
        }
    }

    // ---------- RECORDED MODE ----------
    private fun uploadRecordedVideo(path: String) {
        viewModelScope.launch {
            val kycId = currentKycId()
            android.util.Log.d("KYC", "upload kycId='$kycId' path='$path'")

            if (kycId.isBlank()) {
                _uiState.update { it.copy(videoUploadError = "Missing KYC id — reload status") }
                return@launch
            }

            _uiState.update {
                it.copy(
                    videoPath = path,
                    videoUploading = true,
                    videoUploadError = null
                )
            }
            when (val res = videoRepository.uploadVideo(path, kycId = currentKycId())) {
                is ResponseState.Success -> pollUntilDone(res.data.jobId)
                is ResponseState.Error ->
                    _uiState.update {
                        it.copy(
                            videoUploading = false,
                            videoUploadError = res.message
                        )
                    }

                else -> Unit
            }
        }
    }

    private suspend fun pollUntilDone(jobId: String) {
        repeat(20) {
            when (val res = videoRepository.pollJobStatus(jobId)) {
                is ResponseState.Success -> when (res.data.status) {
                    "APPROVED" -> {
                        _uiState.update {
                            it.copy(
                                videoUploading = false,
                                livenessVerified = true
                            )
                        }; return
                    }

                    "REJECTED" -> {
                        _uiState.update {
                            it.copy(
                                videoUploading = false,
                                videoUploadError = res.data.reason ?: "Verification failed"
                            )
                        }; return
                    }

                    else -> Unit
                }

                is ResponseState.Error -> {
                    _uiState.update {
                        it.copy(
                            videoUploading = false,
                            videoUploadError = res.message
                        )
                    }; return
                }

                else -> Unit
            }
            delay(3000)
        }
        _uiState.update {
            it.copy(
                videoUploading = false,
                videoUploadError = "Verification timed out"
            )
        }
    }

    // ---------- LIVE MODE ----------
    private fun startLiveness() {
        livenessJob?.cancel()
        livenessJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    liveness = it.liveness.copy(
                        connecting = true,
                        error = null
                    )
                )
            }
            val session = when (val res = videoRepository.startLivenessSession(Build.MODEL)) {
                is ResponseState.Success -> res.data
                is ResponseState.Error -> {
                    _uiState.update {
                        it.copy(
                            liveness = it.liveness.copy(
                                connecting = false,
                                error = res.message
                            )
                        )
                    }
                    return@launch
                }

                else -> return@launch
            }
            socketClient.connect(session.wsUrl, session.token).collect { ev ->
                when (ev) {
                    is LivenessSocketEvent.Message -> handleServerMessage(ev.msg)
                    is LivenessSocketEvent.Failure ->
                        _uiState.update {
                            it.copy(
                                liveness = it.liveness.copy(
                                    connecting = false,
                                    error = ev.error
                                )
                            )
                        }

                    LivenessSocketEvent.Closed ->
                        _uiState.update { it.copy(liveness = it.liveness.copy(connecting = false)) }
                }
            }
        }
    }

    private fun handleServerMessage(msg: LivenessServerMessage) {
        when (msg.type) {
            "CHALLENGE" -> _uiState.update {
                it.copy(
                    liveness = it.liveness.copy(
                        connecting = false,
                        activeChallenge = msg.challenge?.toPrompt(),
                        step = msg.step ?: it.liveness.step,
                        total = msg.total ?: it.liveness.total
                    )
                )
            }

            "PROGRESS" -> _uiState.update {
                it.copy(liveness = it.liveness.copy(step = msg.step ?: it.liveness.step))
            }

            "RESULT" -> _uiState.update {
                it.copy(
                    livenessVerified = msg.passed == true,
                    liveness = it.liveness.copy(
                        connecting = false,
                        activeChallenge = null,
                        error = if (msg.passed == true) null else (msg.message ?: "Liveness failed")
                    )
                )
            }

            "ERROR" -> _uiState.update {
                it.copy(liveness = it.liveness.copy(connecting = false, error = msg.message))
            }
        }
    }

    private fun stopLiveness() {
        socketClient.abort()
        livenessJob?.cancel()
        _uiState.update {
            it.copy(
                liveness = it.liveness.copy(
                    connecting = false,
                    activeChallenge = null
                )
            )
        }
    }

    private fun String.toPrompt(): String = when (this) {
        "BLINK" -> "Blink slowly"
        "TURN_LEFT" -> "Turn your head left"
        "TURN_RIGHT" -> "Turn your head right"
        "SMILE" -> "Smile"
        else -> "Follow the prompt"
    }

    private fun currentKycId(): String =
        (uiState.value.statusResponse as? ResponseState.Success)?.data?.kycId ?: ""

    fun fetchStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(statusResponse = ResponseState.Loading) }
            val res = runCatching { repository.getKycStatus() }
            res.fold(
                onSuccess = { base ->
                    Log.d("KYC", "getKycStatus apiStatus=${base.apiStatus} msg='${base.apiMessage}' err='${base.error}'")
                    _uiState.update {
                        it.copy(
                            statusResponse = if (base.apiStatus) {
                                ResponseState.Success(base.apiData)
                            } else {
                                ResponseState.Error(base.apiMessage.ifBlank { base.error })
                            }
                        )
                    }
                },
                onFailure = { e ->
                    Log.e("KYC", "getKycStatus threw", e)
                    _uiState.update {
                        it.copy(
                            statusResponse = ResponseState.Error(
                                UserMessages.of(e, "Couldn't load status")
                            )
                        )
                    }
                }
            )
        }
    }

    private fun submit() {
        val s = uiState.value

        // Resolve the catalog id -> API document type. Bail with an error if unmapped.
        val docType = mapToApiDocumentType(s.selectedDocTypeId.orEmpty())
        if (docType == null) {
            _uiState.update {
                it.copy(submitResponse = ResponseState.Error("Please select a valid document type"))
            }
            return
        }

        val request = KycSubmitRequest(
            fullName = s.fullName,
            dateOfBirth = s.dateOfBirth,
            nationality = s.nationality,
            addressLine = s.addressLine,
            city = s.city,
            postalCode = s.postalCode,
            country = s.country,
            proofOfAddressPath = s.proofOfAddressPath.ifBlank { null },
            documentType = docType.apiValue,
            documentNumber = s.documentNumber,
            frontImagePath = s.frontImagePath,
            backImagePath = s.backImagePath.ifBlank { null },
            selfieImagePath = s.selfieImagePath,
            videoPath = s.videoPath.ifBlank { null },
            livenessVerified = s.livenessVerified,
            consentAccepted = s.consentAccepted
        )

        viewModelScope.launch {
            _uiState.update { it.copy(submitResponse = ResponseState.Loading) }
            val res = runCatching { repository.submitKyc(request) }
            res.fold(
                onSuccess = { base ->
                    Log.d("KYC", "getKycStatus apiStatus=${base.apiStatus} msg='${base.apiMessage}' err='${base.error}'")
                    // BaseResponse -> ResponseState. Adjust the success/failure check
                    // to match your BaseResponse shape (status / success / code).
                    _uiState.update {
                        it.copy(
                            submitResponse = if (base.apiStatus) {
                                ResponseState.Success(base.apiData)
                            } else {
                                ResponseState.Error(base.apiMessage.ifBlank { base.error })
                            }
                        )
                    }
                },
                onFailure = { e ->
                    Log.e("KYC", "getKycStatus threw", e)
                    _uiState.update {
                        it.copy(
                            submitResponse = ResponseState.Error(
                                UserMessages.of(e, "Something went wrong")
                            )
                        )
                    }
                }
            )
        }
    }

    private fun mapToApiDocumentType(catalogId: String): KycDocumentType? =
        KycDocumentType.fromApiValue(catalogId)

    override fun onCleared() {
        socketClient.abort()
        livenessJob?.cancel()
        super.onCleared()
    }
}
