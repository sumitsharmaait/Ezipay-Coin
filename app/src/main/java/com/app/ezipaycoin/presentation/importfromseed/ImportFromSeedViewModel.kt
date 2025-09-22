package com.app.ezipaycoin.presentation.importfromseed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.domain.repository.AuthRepository
import com.app.ezipaycoin.presentation.App
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.WalletManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wallet.core.jni.CoinType
import wallet.core.jni.Curve
import wallet.core.jni.HDWallet
import wallet.core.jni.Hash
import wallet.core.jni.PrivateKey
import java.security.InvalidParameterException

class ImportFromSeedViewModel(private val repository: AuthRepository) : ViewModel() {


    private val _uiState = MutableStateFlow(ImportFromSeedState())
    val uiState: StateFlow<ImportFromSeedState> = _uiState.asStateFlow()


    private val _eventFlow = MutableSharedFlow<ImportFromSeedVMEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: ImportFromSeedEvent) {
        when (event) {
            is ImportFromSeedEvent.SeedPhraseEntered -> {
                _uiState.update {
                    it.copy(seedPhrase = event.seeds)
                }
            }

            is ImportFromSeedEvent.ConfirmPasswordChange -> {
                _uiState.update {
                    it.copy(confirmPassword = event.confirmPassword)
                }
            }

            is ImportFromSeedEvent.ConfirmPasswordToggle -> {
                _uiState.update {
                    it.copy(confirmPasswordVisible = event.confirmPasswordToggle)
                }
            }

            is ImportFromSeedEvent.NewPasswordChange -> {
                _uiState.update {
                    it.copy(newPassword = event.newPassword)
                }
            }

            is ImportFromSeedEvent.NewPasswordToggle -> {
                _uiState.update {
                    it.copy(newPasswordVisible = event.passwordToggle)
                }
            }

            is ImportFromSeedEvent.SignInWithFaceIdChange -> {
                _uiState.update {
                    it.copy(signInWithFaceId = event.isFaceIdChange)
                }
            }

            ImportFromSeedEvent.ImportBtnClick -> {
                if (validateForm()) {
                    try {
                        val wallet = WalletManager.importWallet(_uiState.value.seedPhrase.trim())
                        getNonce(wallet)
                    } catch (ex: InvalidParameterException) {
                        _uiState.update {
                            it.copy(seedPhraseError = "Invalid seed phrase")
                        }
                    }
                }
            }
        }

    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getNonce(wallet: HDWallet) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    responseState = ResponseState.Loading,
                )
            }
            try {
                val privateKey = wallet.getKeyForCoin(CoinType.ETHEREUM)
                val address = CoinType.ETHEREUM.deriveAddress(privateKey)
                val res = repository.getNonce(address)
                if (res.apiStatus) {
                    val signatureHex = signNonceWithTrustWalletCore(res.apiData.nonce, privateKey)
                    val loginRes = repository.postLogin(LoginRequest(address, signatureHex, "2"))
                    if (loginRes.apiStatus) {
                        val dataStore = App.getInstance().dataStore
                        dataStore.updateData {
                            UserPreferences(
                                isWalletCreated = true,
                                password = _uiState.value.newPassword,
                                walletAddress = address,
                                walletPrivateKey = privateKey.data()
                                    .toHexString(format = HexFormat.Default),
                                nonce = res.apiData.nonce,
                                token = loginRes.apiData.token,
                                userName = loginRes.apiData.user.name,
                                userEmail = loginRes.apiData.user.email,
                                userProfile = loginRes.apiData.user.profilePic,
                                seedPhrase = wallet.mnemonic()
                            )
                        }
                        _uiState.update {
                            it.copy(
                                responseState = ResponseState.Success(res)
                            )
                        }
                        _eventFlow.emit(ImportFromSeedVMEvent.MoveToSuccess)
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

    private fun validateForm(): Boolean {

        _uiState.update {
            it.copy(
                seedPhraseError = "",
                newPasswordError = "",
                confirmPasswordError = ""
            )
        }
        if (_uiState.value.seedPhrase.isBlank()) {
            _uiState.update {
                it.copy(seedPhraseError = "Please Enter seed phrases")
            }
            return false
        }

        if (_uiState.value.seedPhrase.trim().split(" ").size != 12) {
            _uiState.update {
                it.copy(seedPhraseError = "Seed words are not correct")
            }
            return false
        }

        if (_uiState.value.newPassword.isBlank() && _uiState.value.newPassword.length < 8) {
            _uiState.update {
                it.copy(newPasswordError = "Must be at least 8 characters")
            }
            return false
        }

        if (_uiState.value.confirmPassword.isBlank() && _uiState.value.confirmPassword.length < 8) {
            _uiState.update {
                it.copy(confirmPasswordError = "Must be at least 8 characters")
            }
            return false
        }

        if (!_uiState.value.newPassword.equals(_uiState.value.confirmPassword, false)) {
            _uiState.update {
                it.copy(
                    confirmPasswordError = "Passwords are not same",
                    newPasswordError = "Passwords are not same"
                )
            }
            return false
        }

        return true
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun signNonceWithTrustWalletCore(nonce: String, privateKey: PrivateKey): String {

        // Ethereum-style signed message prefix
        val prefix = "\u0019Ethereum Signed Message:\n" + nonce.length
        val prefixedMessage = prefix + nonce

        // Hash the message using Keccak-256
        val messageHash = Hash.keccak256(prefixedMessage.toByteArray())

        // Sign the message hash
        val signature = privateKey.sign(messageHash, Curve.SECP256K1)

        // Return hex representation of the signature
        return signature.toHexString()
    }

}