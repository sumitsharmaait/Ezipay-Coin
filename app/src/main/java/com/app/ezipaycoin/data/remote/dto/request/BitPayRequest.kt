package com.app.ezipaycoin.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class BitPayRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: List<String>,
    val id: String = "1"
)
