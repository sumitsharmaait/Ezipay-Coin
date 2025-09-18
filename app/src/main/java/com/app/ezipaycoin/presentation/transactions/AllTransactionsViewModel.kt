package com.app.ezipaycoin.presentation.transactions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.domain.repository.TransactionsRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AllTransactionsViewModel(private val repository: TransactionsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsState())
    val uiState: StateFlow<TransactionsState> = _uiState.asStateFlow()

    init {
        loadMoreTransactions()
    }

    fun onEvent(event: TransactionsEvent) {
        when (event) {
            is TransactionsEvent.FilterChanged -> {
                _uiState.update {
                    it.copy(
                        selectedFilter = event.filter,
                        currentPage = 0,
                        isLoading = false,
                        transactionsList = emptyList(),
                        canLoadMore = true,
                        responseState = ResponseState.Idle
                    )
                }
                loadMoreTransactions()
            }
        }
    }

    fun loadMoreTransactions() {
        if (_uiState.value.isLoading || !_uiState.value.canLoadMore) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }
            try {
                val transactionsRequest = TransactionsRequest(
                    chain = "BSC",
                    size = 10,
                    page = _uiState.value.currentPage,
                    type = _uiState.value.selectedFilter.lowercase()
                )
                val res = repository.getTransactions(transactionsRequest)
                if (res.apiStatus) {
                    if (res.apiData.items.isNotEmpty()) {
                        val totalTransactions = _uiState.value.transactionsList + res.apiData.items
                        val canLoadMore = totalTransactions.size != res.apiData.total
                        _uiState.update {
                            it.copy(
                                responseState = ResponseState.Success(res),
                                transactionsList = totalTransactions,
                                isLoading = false,
                                currentPage = it.currentPage++,
                                canLoadMore = canLoadMore
                            )
                        }
                    }
                    Log.e("", "loadMoreTransactions: "+_uiState.value )
                } else {
                    _uiState.update {
                        it.copy(
                            responseState = ResponseState.Error(
                                res.apiMessage
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        responseState = ResponseState.Error(
                            e.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }

}