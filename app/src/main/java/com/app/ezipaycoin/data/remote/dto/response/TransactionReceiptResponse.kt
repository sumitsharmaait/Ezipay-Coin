package com.app.ezipaycoin.data.remote.dto.response

data class TransactionReceiptResponse(
    val jsonrpc: String,
    val id: String,
    val result: ReceiptResult?,
    val error: BnbChainResponse.ErrorBnbChain?
) {
    data class ReceiptResult(
        val status: String
    )
}
