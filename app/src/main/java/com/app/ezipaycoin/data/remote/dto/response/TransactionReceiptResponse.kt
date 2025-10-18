package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TransactionReceiptResponse(
    val jsonrpc: String,
    val id: String,
    val result: ReceiptResult?,
    val error: BnbChainResponse.ErrorBnbChain?
) {
    @Serializable
    data class ReceiptResult(
        val status: String
    )
}
