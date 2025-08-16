package com.app.ezipaycoin.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.presentation.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SplashState())
    val uiState: StateFlow<SplashState> = _uiState

    init {
        viewModelScope.launch {
            val prefs = App.getInstance().dataStore.data.first()
            val isWalletCreated = prefs.isWalletCreated
            _uiState.value = _uiState.value.copy(
                isLoggedIn = isWalletCreated
            )
        }
    }

    fun onEvent(event: SplashEvent) {

    }

}