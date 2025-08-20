package com.app.ezipaycoin.presentation.main

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.presentation.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState

    private var isLoading = mutableStateOf(true)


    init {
        viewModelScope.launch {
           // delay(1000)
            val prefs = App.getInstance().dataStore.data.first()
            val isWalletCreated = prefs.isWalletCreated
          //  SessionManager.token = prefs.token
            isLoading.value = false
            _uiState.value = _uiState.value.copy(
                isLoggedIn = isWalletCreated,
                dataLoaded = true
            )
        }
    }

    fun onEvent(event: MainEvent) {
        _uiState.value = when (event) {
            is MainEvent.BottomBarClicked -> _uiState.value.copy(selectedBottomBar = event.bottomBar)
        }
    }

}