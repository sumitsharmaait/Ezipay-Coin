package com.app.ezipaycoin.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.domain.repository.TransactionsRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.CancellationException
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
                        currentPage = FIRST_PAGE,
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

    /** Retries the page that failed. */
    fun retry() {
        _uiState.update { it.copy(responseState = ResponseState.Idle) }
        loadMoreTransactions()
    }

    fun loadMoreTransactions() {
        if (_uiState.value.isLoading || !_uiState.value.canLoadMore) return

        // Captured so a filter change while a request is in flight drops the stale answer.
        val filter = _uiState.value.selectedFilter
        val page = _uiState.value.currentPage

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val res = repository.getTransactions(
                    TransactionsRequest(
                        chain = "BSC",
                        size = PAGE_SIZE,
                        page = page,
                        type = filter.lowercase()
                    )
                )
                if (_uiState.value.selectedFilter != filter) return@launch
                if (res.apiStatus) {
                    val items = res.apiData.items
                    val all = _uiState.value.transactionsList + items
                    // The last page is short, or the server total has been reached.
                    val canLoadMore = items.size >= PAGE_SIZE && all.size < res.apiData.total
                    _uiState.update {
                        it.copy(
                            responseState = if (all.isEmpty()) {
                                ResponseState.Error(EMPTY_MESSAGE)
                            } else {
                                ResponseState.Success(res)
                            },
                            transactionsList = all,
                            isLoading = false,
                            currentPage = if (items.isNotEmpty()) page + 1 else page,
                            canLoadMore = items.isNotEmpty() && canLoadMore
                        )
                    }
                } else {
                    fail(filter, res.apiMessage)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fail(filter, e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // The page number is not advanced, so retry() asks for the same page again.
    private fun fail(filter: String, message: String) {
        if (_uiState.value.selectedFilter != filter) return
        _uiState.update {
            it.copy(isLoading = false, responseState = ResponseState.Error(message))
        }
    }

    companion object {
        // The backend counts pages from 1 (0 is treated as 1 as well).
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 10
        const val EMPTY_MESSAGE = "No Transactions Found!!"
    }
}
