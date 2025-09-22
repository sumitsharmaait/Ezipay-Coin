package com.app.ezipaycoin.presentation.confirmseedphrase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.domain.repository.AuthRepository
import com.app.ezipaycoin.presentation.App
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.SnackbarController
import com.app.ezipaycoin.utils.SnackbarEvent
import com.app.ezipaycoin.utils.WalletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wallet.core.jni.CoinType
import wallet.core.jni.Curve
import wallet.core.jni.Hash
import wallet.core.jni.PrivateKey

class ConfirmSeedPhraseViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmSeedPhraseState())
    val uiState: StateFlow<ConfirmSeedPhraseState> = _uiState.asStateFlow()
    private var originalSeeds: List<String> = emptyList()

    fun createDistract(originalSeed: List<String>, random: Int) {
        originalSeeds = originalSeed
        val correctWord = originalSeed[random - 1]
        val remainingWords =
            originalSeed.filterNot { it == correctWord || _uiState.value.confirmedSeeds.contains(it) }
        val distracts = remainingWords.shuffled().take(5)
        val distractsWithCorrect = (distracts + correctWord).shuffled()
        _uiState.update {
            it.copy(
                currentConfirmationIndex = random,
                distractSeeds = distractsWithCorrect
            )
        }
    }

    fun onEvent(event: ConfirmSeedPhraseEvent) {
        when (event) {
            is ConfirmSeedPhraseEvent.OnVerifySeedClick -> {
                if (event.selectedSeed == originalSeeds.getOrNull(_uiState.value.currentConfirmationIndex - 1)) {
                    //seed verified
                    val seeds: MutableList<String> = mutableListOf()
                    if (_uiState.value.confirmedSeeds.isNotEmpty()) {
                        seeds.addAll(_uiState.value.confirmedSeeds)
                    }
                    seeds.add(event.selectedSeed)
                    _uiState.update { it.copy(confirmedSeeds = seeds) }

                    if (_uiState.value.confirmedSeeds.size == 3) {
                        if (WalletManager.isInitialized()) {
                            val wallet = WalletManager.wallet
                            val privateKey = wallet.getKeyForCoin(CoinType.ETHEREUM)
                            val address = CoinType.ETHEREUM.deriveAddress(privateKey)
                            getNonce(address, privateKey)
                        }
                    } else {
                        createDistract(originalSeeds, (4..8).random())
                    }
                } else {
                    showSnackbar("Seed is not correct, Please select correct word!!")
                }
            }
        }
    }


    @OptIn(ExperimentalStdlibApi::class)
    private fun getNonce(publicKey: String, privateKey: PrivateKey) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    responseState = ResponseState.Loading,
                    isAllConfirmed = true
                )
            }
            try {
                val res = repository.getNonce(publicKey)
                if (res.apiStatus) {
                    val signatureHex = signNonceWithTrustWalletCore(res.apiData.nonce, privateKey)
                    val loginRes = repository.postLogin(LoginRequest(publicKey, signatureHex, "2"))
                    if (loginRes.apiStatus) {
                        val dataStore = App.getInstance().dataStore
                        dataStore.updateData {
                            UserPreferences(
                                isWalletCreated = true,
                                walletAddress = publicKey,
                                walletPrivateKey = privateKey.data()
                                    .toHexString(format = HexFormat.Default),
                                nonce = res.apiData.nonce,
                                token = loginRes.apiData.token,
                                userName = loginRes.apiData.user.name,
                                userEmail = loginRes.apiData.user.email,
                                userProfile = loginRes.apiData.user.profilePic
                            )
                        }

                        _uiState.update {
                            it.copy(
                                isAllConfirmed = true,
                                responseState = ResponseState.Success(res)
                            )
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
                        isAllConfirmed = true,
                        responseState = ResponseState.Error(
                            e.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }

    fun showSnackbar(msg: String) {
        viewModelScope.launch {
            SnackbarController.sendEvent(
                event = SnackbarEvent(
                    message = msg,
                )
            )
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun signNonceWithTrustWalletCore(nonce: String, privateKey: PrivateKey): String {

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