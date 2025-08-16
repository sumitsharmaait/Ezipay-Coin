package com.app.ezipaycoin.presentation.seedphraseview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.presentation.App
import com.app.ezipaycoin.data.repository.AuthRepoImpl
import com.app.ezipaycoin.utils.SnackbarController
import com.app.ezipaycoin.utils.SnackbarEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ViewSeedPhraseViewModel(private val repository: AuthRepoImpl) : ViewModel() {

    private val _uiState = MutableStateFlow(SeedPhraseState())
    val uiState: StateFlow<SeedPhraseState> = _uiState

    init {
        viewModelScope.launch {
            val wallet = App.getInstance().wallet
            _uiState.value = _uiState.value.copy(seedWords = wallet.mnemonic().split(" "))
        }
    }

    fun onEvent(event: SeedPhraseEvent) {
        when (event) {
            is SeedPhraseEvent.viewPhrases -> {
                _uiState.value = _uiState.value.copy(isViewed = true)
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


}