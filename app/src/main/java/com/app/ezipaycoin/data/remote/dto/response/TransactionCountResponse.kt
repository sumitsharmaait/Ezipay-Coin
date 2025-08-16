package com.app.ezipaycoin.data.remote.dto.response

data class BnbChainResponse(
    val jsonrpc: String,
    val id: String,
    val result: String?,
    val error: ErrorBnbChain?
) {
    class ErrorBnbChain(
        val code: Double,
        val message: String
    )
}
