package com.app.ezipaycoin.presentation.createwith

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.data.remote.dto.request.UpdateProfileRequest
import com.app.ezipaycoin.domain.repository.AuthRepository
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.SessionManager
import com.app.ezipaycoin.utils.WalletManager
import com.app.ezipaycoin.utils.WalletManager.signNonceWithTrustWalletCore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet

class WalletSetupVM(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletSetupState())
    val uiState: StateFlow<WalletSetupState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<WalletSetupVMEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val eventFlow = _eventFlow.asSharedFlow()


    fun onEvent(event: WalletSetupEvent) {
        when (event) {
            is WalletSetupEvent.SocialDetailsFetched -> {
                val wallet = WalletManager.createNewWallet()
                getNonce(wallet, event.name, event.email, event.profilePic)
            }

            is WalletSetupEvent.DismissDialog -> {
                _uiState.update {
                    it.copy(responseState = ResponseState.Idle)
                }
            }

            is WalletSetupEvent.SocialSignInLoading -> {
                _uiState.update {
                    it.copy(loadingSocialSignIn = !_uiState.value.loadingSocialSignIn)
                }
            }

        }

    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getNonce(wallet: HDWallet, name: String, email: String, pic: String) {
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
                val privateKey = wallet.getKeyForCoin(CoinType.ETHEREUM)
                val address = CoinType.ETHEREUM.deriveAddress(privateKey)
                val res = repository.getNonce(address)
                if (res.apiStatus) {
                    val signatureHex = signNonceWithTrustWalletCore(res.apiData.nonce, privateKey)
                    val loginRes = repository.postLogin(LoginRequest(address, signatureHex, token))
                    if (loginRes.apiStatus) {
                        SessionManager.token = loginRes.apiData.token
                        val updateProfileRes =
                            repository.updateProfile(UpdateProfileRequest(name, email))
                        if (updateProfileRes.apiStatus) {
                            val dataStore = UserPreferencesRepository.userPreferencesFlow
                            dataStore.updateData { prefs ->
                                prefs.copy(
                                    isWalletCreated = true,
                                    walletAddress = address,
                                    walletPrivateKey = privateKey.data()
                                        .toHexString(format = HexFormat.Default),
                                    nonce = res.apiData.nonce,
                                    token = loginRes.apiData.token,
                                    userName = name,
                                    userEmail = email,
                                    userProfile = loginRes.apiData.user.profilePic ?: pic,
                                    seedPhrase = wallet.mnemonic()
                                )
                            }
                            _uiState.update {
                                it.copy(
                                    responseState = ResponseState.Success(res)
                                )
                            }
                            _eventFlow.emit(WalletSetupVMEvent.MoveToSuccess)
                        } else {
                            _uiState.update {
                                it.copy(
                                    responseState = ResponseState.Error(
                                        updateProfileRes.apiMessage
                                    )
                                )
                            }
                        }
                    }

                } else {
                    _uiState.update {
                        it.copy(
                            responseState = ResponseState.Error(
                                res.apiMessage
                            )
                        )
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

}