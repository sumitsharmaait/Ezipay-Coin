package com.app.ezipaycoin.presentation.viewwalletdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.utils.PasswordCredentials
import com.app.ezipaycoin.utils.PasswordGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The seed phrase and private key are loaded only after the app password is entered again, and are
 * dropped from memory when the screen goes to the background.
 */
class ViewWalletDetailsVM : ViewModel() {

    private val _uiState = MutableStateFlow(ViewWalletDetailsState())
    val uiState: StateFlow<ViewWalletDetailsState> = _uiState.asStateFlow()

    private val gate = PasswordGate()

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = "") }
    }

    fun toggleKeyReveal() {
        _uiState.update { it.copy(privateKeyRevealed = !it.privateKeyRevealed) }
    }

    fun unlock() {
        viewModelScope.launch {
            val prefs = UserPreferencesRepository.userPreferencesFlow.data.first()
            val entered = _uiState.value.password
            val matched = withContext(Dispatchers.Default) { PasswordCredentials.matches(prefs, entered) }
            when (val result = gate.check(entered) { matched }) {
                is PasswordGate.Result.Ok -> _uiState.update {
                    it.copy(
                        unlocked = true,
                        password = "",
                        passwordError = "",
                        privateKey = prefs.walletPrivateKey ?: "Not Found",
                        seedPhrase = prefs.seedPhrase ?: "Not Found"
                    )
                }

                is PasswordGate.Result.Wrong -> _uiState.update {
                    it.copy(password = "", passwordError = "Incorrect password")
                }

                is PasswordGate.Result.LockedOut -> _uiState.update {
                    it.copy(
                        password = "",
                        passwordError = "Too many attempts. Try again in ${result.retryAfterMillis / 1000 + 1} seconds"
                    )
                }
            }
        }
    }

    /** Hides the secrets again and requires the password for the next reveal. */
    fun lock() {
        _uiState.value = ViewWalletDetailsState()
    }
}
