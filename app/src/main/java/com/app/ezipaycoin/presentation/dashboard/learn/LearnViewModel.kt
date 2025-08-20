package com.app.ezipaycoin.presentation.dashboard.learn

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LearnViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LearnState())
    val uiState: StateFlow<LearnState> = _uiState



    init {

    }



}