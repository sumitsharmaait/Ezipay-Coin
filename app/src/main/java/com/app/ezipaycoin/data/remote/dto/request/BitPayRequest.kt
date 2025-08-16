package com.app.ezipaycoin.data.remote.dto.request

data class BitPayRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: List<String>,
    val id: String = "1"
)
