package com.app.ezipaycoin.presentation.dashboard.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.domain.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalletViewModel(private val repository: HomeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletState())
    val uiState: StateFlow<WalletState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {

        }
    }

    fun onEvent(event: WalletEvent) {
        when (event) {
            is WalletEvent.ExpandCurrencyOptions -> {
                _uiState.update { it.copy(isCurrencyOptionsExpanded = event.isExpand) }
            }

            is WalletEvent.CurrentOptionChanged -> {
                _uiState.update {
                    it.copy(
                        isCurrencyOptionsExpanded = !_uiState.value.isCurrencyOptionsExpanded
                    )
                }
            }
        }
    }

}