package com.app.ezipaycoin.presentation.main

import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.CorruptionException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.utils.DataStorePreferencesAccess
import com.app.ezipaycoin.utils.PasswordCredentials
import com.app.ezipaycoin.utils.PasswordMigration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState

    private val store = DataStorePreferencesAccess(UserPreferencesRepository.userPreferencesFlow)

    private var isLoading = mutableStateOf(true)


    init {
        viewModelScope.launch {
            val prefs = try {
                UserPreferencesRepository.userPreferencesFlow.data.first()
            } catch (e: CorruptionException) {
                isLoading.value = false
                _uiState.update { it.copy(storageError = true, dataLoaded = true) }
                return@launch
            }
            val isWalletCreated = prefs.isWalletCreated
            isLoading.value = false
            _uiState.value = _uiState.value.copy(
                isLoggedIn = isWalletCreated,
                dataLoaded = true
            )
            if (PasswordCredentials.hasPassword(prefs)) {
                _uiState.update { it.copy(passwordCreated = true) }
            }
            // Replaces a plaintext password left by an older version with a verifier.
            try {
                PasswordMigration(store).run()
            } catch (e: Exception) {
                // Nothing was lost: the plaintext stays until a later start succeeds.
            }
        }
    }

    /** The stored wallet is unreadable: keep a copy of it and send the user to the seed import. */
    fun startRecovery() {
        if (UserPreferencesRepository.beginRecovery()) {
            _uiState.update {
                it.copy(storageError = false, isLoggedIn = false, passwordCreated = false)
            }
        }
    }

    fun onEvent(event: MainEvent) {
        _uiState.value = when (event) {
            is MainEvent.BottomBarClicked -> _uiState.value.copy(selectedBottomBar = event.bottomBar)
        }
    }

}