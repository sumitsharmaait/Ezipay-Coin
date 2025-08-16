package com.app.ezipaycoin.data.remote.dto.request

data class TransactionsRequest(
    val chain: String = "BSC",
    val type: String = "all",
    val page: Int = 1,
    val size: Int = 2
)
