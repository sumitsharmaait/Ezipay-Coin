package com.app.ezipaycoin.presentation.confirmseedphrase

import android.util.Log
import com.app.ezipaycoin.utils.UserMessages
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.domain.repository.AuthRepository
import com.app.ezipaycoin.utils.FcmToken
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.SeedHolder
import com.app.ezipaycoin.utils.SessionManager
import com.app.ezipaycoin.utils.WalletSignIn
import com.app.ezipaycoin.utils.SnackbarController
import com.app.ezipaycoin.utils.SnackbarEvent
import com.app.ezipaycoin.utils.WalletManager
import com.google.firebase.messaging.FirebaseMessaging
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


    /** Tries the sign-in again after a failure; the wallet is still only in memory. */
    fun retry() {
        if (_uiState.value.responseState is ResponseState.Loading || !WalletManager.isInitialized()) return
        val privateKey = WalletManager.wallet.getKeyForCoin(CoinType.ETHEREUM)
        getNonce(CoinType.ETHEREUM.deriveAddress(privateKey), privateKey)
    }

    override fun onCleared() {
        SeedHolder.clear()
        super.onCleared()
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
                val result = WalletSignIn(repository).run(publicKey, FcmToken.fetch()) { nonce ->
                    signNonceWithTrustWalletCore(nonce, privateKey)
                }
                when (result) {
                    is WalletSignIn.Result.Success -> {
                        val loginRes = result.login
                        val dataStore = UserPreferencesRepository.userPreferencesFlow
                        dataStore.updateData {
                            it.copy(
                                isWalletCreated = true,
                                walletAddress = publicKey,
                                walletPrivateKey = privateKey.data()
                                    .toHexString(format = HexFormat.Default),
                                nonce = result.nonce,
                                token = loginRes.apiData.token,
                                userName = loginRes.apiData.user.name,
                                userEmail = loginRes.apiData.user.email,
                                userProfile = loginRes.apiData.user.profilePic ?: WalletManager.pendingProfilePic,
                                seedPhrase = originalSeeds.joinToString(separator = " ")
                            )
                        }
                        SessionManager.token = loginRes.apiData.token
                        WalletManager.pendingProfilePic = null
                        SeedHolder.clear()
                        _uiState.update {
                            it.copy(
                                isAllConfirmed = true,
                                responseState = ResponseState.Success(loginRes)
                            )
                        }
                    }

                    is WalletSignIn.Result.Failed -> _uiState.update {
                        it.copy(isAllConfirmed = true, responseState = ResponseState.Error(result.message))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAllConfirmed = true,
                        responseState = ResponseState.Error(
                            UserMessages.of(e, "Unknown error")
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