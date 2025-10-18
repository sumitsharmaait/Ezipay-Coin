package com.app.ezipaycoin.presentation.unlock

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.domain.repository.AuthRepository
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.SessionManager
import com.app.ezipaycoin.utils.WalletManager.signNonceWithTrustWalletCore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wallet.core.jni.PrivateKey

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
                var token = ""
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        token = task.result
                        Log.d("FCM", "Current token: $token")
                    }
                }
                dataStoreData?.walletAddress?.let { wallet ->
                    val res = repository.getNonce(wallet)
                    if (res.apiStatus) {
                        val signatureHex = signNonceWithTrustWalletCore(
                            res.apiData.nonce,
                            PrivateKey(
                                dataStoreData?.walletPrivateKey?.removePrefix("0x")
                                    ?.hexToByteArray()
                            )
                        )
                        val loginRes =
                            repository.postLogin(LoginRequest(wallet, signatureHex, token))
                        if (loginRes.apiStatus) {
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
                        }
                    }

                }


            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        responseState = ResponseState.Error(
                            e.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }

        }

    }

    private fun verifyPassword() {
        viewModelScope.launch {
            dataStoreData?.password?.let {
                if (it.contentEquals(_uiState.value.password)) {
                    postLogin()
                    return@launch
                }
            }
            _uiState.update {
                it.copy(passwordError = "Incorrect password")
            }
        }
    }

}