package com.app.ezipaycoin.presentation.transactions

import androidx.compose.ui.unit.IntSize
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.utils.ResponseState

data class TransactionsState(
    var responseState: ResponseState<BaseResponse<TransactionsResponse>> = ResponseState.Idle,
    val filters: List<String> = listOf("All", "Send", "Received"),
    var selectedFilter: String = "All",
    var scrollbarSize: IntSize = IntSize.Zero,
    var isLoading: Boolean = false,
    var canLoadMore: Boolean = true,
    var currentPage: Int = 1,
    var transactionsList: List<TransactionsResponse.TransactionsItem> = emptyList()
)
