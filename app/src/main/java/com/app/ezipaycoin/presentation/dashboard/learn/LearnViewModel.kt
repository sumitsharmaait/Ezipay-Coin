package com.app.ezipaycoin.presentation.dashboard.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.response.LearnItemResponse
import com.app.ezipaycoin.domain.repository.LearnRepository
import com.app.ezipaycoin.presentation.dashboard.earn.EarnViewModel
import com.app.ezipaycoin.presentation.dashboard.earn.Load
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LearnViewModel(private val repository: LearnRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnState())
    val uiState: StateFlow<LearnState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(lessons = Load.Loading) }
        viewModelScope.launch {
            val next: Load<List<LearnItemResponse>> = when (val result = repository.items()) {
                is ResponseState.Success -> Load.Ready(visible(result.data))
                is ResponseState.Error -> Load.Failed(EarnViewModel.describe(result.message))
                else -> Load.Loading
            }
            _uiState.update { it.copy(lessons = next) }
        }
    }

    companion object {
        /** Active items (a missing status counts as active), in the order the server set. */
        fun visible(items: List<LearnItemResponse>): List<LearnItemResponse> =
            items.filter { it.status == null || it.status.equals("active", ignoreCase = true) }
                .filter { !it.title.isNullOrBlank() }
                .sortedWith(compareBy({ it.sortOrder ?: Int.MAX_VALUE }, { it.id ?: Int.MAX_VALUE }))
    }
}
