package com.app.ezipaycoin.presentation.importfromseed

import androidx.lifecycle.ViewModel
import com.app.ezipaycoin.utils.PasswordVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.domain.repository.AuthRepository
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.SeedPhraseRules
import com.app.ezipaycoin.utils.SessionManager
import com.app.ezipaycoin.utils.WalletSignIn
import com.app.ezipaycoin.utils.WalletManager
import com.app.ezipaycoin.utils.WalletManager.signNonceWithTrustWalletCore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet

class ImportFromSeedViewModel(private val repository: AuthRepository) : ViewModel() {


    private val _uiState = MutableStateFlow(ImportFromSeedState())
    val uiState: StateFlow<ImportFromSeedState> = _uiState.asStateFlow()
    val dataStore = UserPreferencesRepository.userPreferencesFlow

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
                updateBiometricEnabled(event.isFaceIdChange)
                _uiState.update {
                    it.copy(
                        signInWithFaceId = event.isFaceIdChange,
                        bioMetricMessage = if (event.isFaceIdChange) "Biometric login enabled!" else "Authentication failed or cancelled")
                }
            }

            ImportFromSeedEvent.ImportBtnClick -> {
                if (validateForm()) {
                    try {
                        val phrase = (SeedPhraseRules.check(_uiState.value.seedPhrase) as SeedPhraseRules.Result.Ok).normalized
                        val wallet = WalletManager.importWallet(phrase)
                        getNonce(wallet)
                    } catch (ex: Exception) {
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
                val result = WalletSignIn(repository).run(address, "2") { nonce ->
                    signNonceWithTrustWalletCore(nonce, privateKey)
                }
                when (result) {
                    is WalletSignIn.Result.Success -> {
                        val loginRes = result.login
                        val verifier = withContext(Dispatchers.Default) { PasswordVerifier.create(_uiState.value.newPassword) }
                        dataStore.updateData {
                            it.copy(
                                isWalletCreated = true,
                                passwordVerifier = verifier,
                                password = null,
                                walletAddress = address,
                                walletPrivateKey = privateKey.data()
                                    .toHexString(format = HexFormat.Default),
                                nonce = result.nonce,
                                token = loginRes.apiData.token,
                                userName = loginRes.apiData.user.name,
                                userEmail = loginRes.apiData.user.email,
                                userProfile = loginRes.apiData.user.profilePic,
                                seedPhrase = wallet.mnemonic()
                            )
                        }
                        SessionManager.token = loginRes.apiData.token
                        _uiState.update { it.copy(responseState = ResponseState.Success(loginRes)) }
                        _eventFlow.emit(ImportFromSeedVMEvent.MoveToSuccess)
                    }

                    is WalletSignIn.Result.Failed -> _uiState.update {
                        it.copy(responseState = ResponseState.Error(result.message))
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
        val phraseCheck = SeedPhraseRules.check(_uiState.value.seedPhrase)
        if (phraseCheck !is SeedPhraseRules.Result.Ok) {
            _uiState.update {
                it.copy(seedPhraseError = SeedPhraseRules.message(phraseCheck))
            }
            return false
        }

        if (_uiState.value.newPassword.length < 8) {
            _uiState.update {
                it.copy(newPasswordError = "Must be at least 8 characters")
            }
            return false
        }

        if (_uiState.value.confirmPassword.length < 8) {
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

    private fun updateBiometricEnabled(faceIdChange: Boolean) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(
                    bioMetricAuthEnabled = faceIdChange
                )
            }
        }
    }

}