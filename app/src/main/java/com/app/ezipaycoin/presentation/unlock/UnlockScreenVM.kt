package com.app.ezipaycoin.presentation.unlock

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.domain.repository.AuthRepository
import com.app.ezipaycoin.utils.PasswordCredentials
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.SessionManager
import com.app.ezipaycoin.utils.WalletManager.signNonceWithTrustWalletCore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import wallet.core.jni.PrivateKey
import kotlin.coroutines.resume

class UnlockScreenVM(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UnlockState())
    val uiState: StateFlow<UnlockState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UnlockScreenVMEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val eventFlow = _eventFlow.asSharedFlow()

    val dataStore = UserPreferencesRepository.userPreferencesFlow
    var dataStoreData: UserPreferences? = null

    init {
        viewModelScope.launch {
            dataStoreData = dataStore.data.first()
            if (dataStoreData?.bioMetricAuthEnabled == true) {
                _uiState.update {
                    it.copy(bioAuthEnabled = true)
                }
            }
        }

    }

    /** Clears the typed password and any previous result when the app locks again. */
    fun reset() {
        _uiState.update { UnlockState(bioAuthEnabled = it.bioAuthEnabled) }
        viewModelScope.launch {
            dataStoreData = dataStore.data.first()
            _uiState.update { it.copy(bioAuthEnabled = dataStoreData?.bioMetricAuthEnabled == true) }
        }
    }

    fun onEvent(event: UnlockScreenEvent) {
        when (event) {
            is UnlockScreenEvent.OnPasswordChange -> {
                _uiState.update {
                    it.copy(password = event.password)
                }
            }

            is UnlockScreenEvent.PasswordToggle -> {
                _uiState.update {
                    it.copy(passwordVisible = !it.passwordVisible)
                }
            }

            is UnlockScreenEvent.OnSubmit -> {
                verifyPassword()
            }

            is UnlockScreenEvent.OnBioAuthSuccess -> {
                postLogin()
            }
        }

    }


    @OptIn(ExperimentalStdlibApi::class)
    private fun postLogin() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    responseState = ResponseState.Loading,
                )
            }

            try {
                val wallet = dataStoreData?.walletAddress
                val privateKey = dataStoreData?.walletPrivateKey
                if (dataStoreData?.isWalletCreated != true || wallet == null || privateKey == null) {
                    // Password was saved but wallet setup never finished; unlock so the
                    // Auth graph can take the user back through setup.
                    _uiState.update { it.copy(responseState = ResponseState.Idle) }
                    _eventFlow.emit(UnlockScreenVMEvent.Unlocked)
                    return@launch
                }

                val token = fetchFcmToken()
                val res = repository.getNonce(wallet)
                if (!res.apiStatus) {
                    showError(res.apiMessage)
                    return@launch
                }
                val signatureHex = signNonceWithTrustWalletCore(
                    res.apiData.nonce,
                    PrivateKey(privateKey.removePrefix("0x").hexToByteArray())
                )
                val loginRes = repository.postLogin(LoginRequest(wallet, signatureHex, token))
                if (!loginRes.apiStatus) {
                    showError(loginRes.apiMessage)
                    return@launch
                }
                SessionManager.token = loginRes.apiData.token
                dataStore.updateData { pref ->
                    pref.copy(
                        token = loginRes.apiData.token,
                        userProfile = loginRes.apiData.user.profilePic
                            ?: dataStoreData?.userProfile
                    )
                }
                _uiState.update {
                    it.copy(
                        responseState = ResponseState.Success(loginRes)
                    )
                }
                _eventFlow.emit(UnlockScreenVMEvent.Unlocked)
            } catch (e: Exception) {
                showError(if (com.app.ezipaycoin.utils.UserMessages.isParseError(e)) com.app.ezipaycoin.utils.UserMessages.UNEXPECTED_RESPONSE else e.localizedMessage)
            }

        }

    }

    private fun showError(message: String?) {
        _uiState.update {
            it.copy(
                responseState = ResponseState.Error(
                    message?.takeIf { msg -> msg.isNotBlank() } ?: "Unable to unlock. Please try again."
                )
            )
        }
    }

    /** Waits for the FCM token; login still proceeds without one. */
    private suspend fun fetchFcmToken(): String = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = if (task.isSuccessful) task.result.orEmpty() else ""
            if (cont.isActive) cont.resume(token)
        }
    }

    private fun verifyPassword() {
        viewModelScope.launch {
            _uiState.update { it.copy(passwordError = "") }
            // Read again: the stored record may have been migrated since this screen was created.
            val prefs = dataStore.data.first()
            dataStoreData = prefs
            val entered = _uiState.value.password
            val ok = withContext(Dispatchers.Default) { PasswordCredentials.matches(prefs, entered) }
            if (ok) {
                postLogin()
                return@launch
            }
            _uiState.update {
                it.copy(passwordError = "Incorrect password")
            }
        }
    }

}