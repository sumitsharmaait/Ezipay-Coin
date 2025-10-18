package com.app.ezipaycoin.presentation.viewwalletdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewWalletDetailsVM : ViewModel() {

    private val _uiState = MutableStateFlow(ViewWalletDetailsState())
    val uiState: StateFlow<ViewWalletDetailsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = UserPreferencesRepository.userPreferencesFlow.data.first()
            _uiState.update {
                it.copy(
                    privateKey = prefs.walletPrivateKey ?: "Not Found",
                    seedPhrase = prefs.seedPhrase ?: "Not Found"
                )
            }
        }
    }

}