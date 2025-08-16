package com.app.ezipaycoin.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.presentation.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyProfileState())
    val uiState: StateFlow<MyProfileState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = App.getInstance().dataStore.data.first()
            prefs.walletAddress?.let { aVal ->
                _uiState.update {
                    it.copy(walletAddress = aVal)
                }
            }
            prefs.userEmail?.let { bVal ->
                prefs.userName?.let { cVal ->
                    _uiState.update {
                        it.copy(name = bVal, email = cVal)
                    }
                }
            }
        }
    }

}