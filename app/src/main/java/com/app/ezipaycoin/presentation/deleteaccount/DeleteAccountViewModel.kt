package com.app.ezipaycoin.presentation.deleteaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.domain.repository.AuthRepository
import com.app.ezipaycoin.utils.PasswordCredentials
import com.app.ezipaycoin.utils.PasswordGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.app.ezipaycoin.utils.SessionManager
import com.app.ezipaycoin.utils.WalletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeleteAccountState(
    val password: String = "",
    val loading: Boolean = false,
    val error: String = "",
    val deleted: Boolean = false
)

class DeleteAccountViewModel(repository: AuthRepository) : ViewModel() {

    private val store = UserPreferencesRepository.userPreferencesFlow

    private val deletion = AccountDeletion(
        passwordMatches = { entered ->
            val prefs = store.data.first()
            withContext(Dispatchers.Default) { PasswordCredentials.matches(prefs, entered) }
        },
        gate = PasswordGate(),
        deleteRemote = { key -> repository.deleteAccount(key) },
        wipeLocal = {
            store.updateData { UserPreferences() }
            SessionManager.token = ""
            WalletManager.clear()
        },
        newKey = { java.util.UUID.randomUUID().toString() }
    )

    private val _uiState = MutableStateFlow(DeleteAccountState())
    val uiState: StateFlow<DeleteAccountState> = _uiState.asStateFlow()

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = "") }
    }

    fun confirmDelete() {
        if (_uiState.value.loading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = "") }
            val result = deletion.run(_uiState.value.password)
            _uiState.update {
                when (result) {
                    is DeletionResult.Deleted -> it.copy(loading = false, deleted = true, password = "")
                    is DeletionResult.WrongPassword -> it.copy(loading = false, password = "", error = "Incorrect password")
                    is DeletionResult.LockedOut -> it.copy(
                        loading = false, password = "",
                        error = "Too many attempts. Try again in ${result.retryAfterMillis / 1000 + 1} seconds"
                    )
                    is DeletionResult.Failed -> it.copy(loading = false, error = result.message)
                }
            }
        }
    }
}
