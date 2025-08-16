package com.app.ezipaycoin.presentation.dashboard.home

import androidx.lifecycle.ViewModel
import com.app.ezipaycoin.domain.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel(private val homeRepository: HomeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()


    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ExpandCryptoOptions -> {
                _uiState.update { it.copy(isCurrencyOptionsExpanded = event.isExpand) }
            }

            is HomeEvent.CryptoOptionChanged -> {
                _uiState.update {
                    it.copy(
                        isCurrencyOptionsExpanded = !_uiState.value.isCurrencyOptionsExpanded
                    )
                }
            }

            is HomeEvent.ComingSoonDialog -> {
                _uiState.update {
                    it.copy(comingSoonDialogue = event.isVisible)
                }
            }
        }
    }


    init {

    }

}