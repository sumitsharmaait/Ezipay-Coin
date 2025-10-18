package com.app.ezipaycoin.presentation.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceiveViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiveState())
    val uiState: StateFlow<ReceiveState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = UserPreferencesRepository.userPreferencesFlow.data.first()
            prefs.walletAddress?.let { aVal ->
                _uiState.update {
                    it.copy(walletAddress = aVal)
                }
            }
        }
    }
}