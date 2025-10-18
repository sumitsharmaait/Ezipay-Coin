package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BnbChainResponse(
    val jsonrpc: String,
    val id: String,
    val result: String?,
    val error: ErrorBnbChain?
) {
    @Serializable
    class ErrorBnbChain(
        val code: Double,
        val message: String
    )
}
