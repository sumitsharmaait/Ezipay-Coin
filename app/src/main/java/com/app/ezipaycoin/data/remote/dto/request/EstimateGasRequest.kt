package com.app.ezipaycoin.data.remote.dto.request

data class EstimateGasRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: List<ParamObj>,
    val id: String = "1"
) {
    data class ParamObj(
        val from: String,
        val to: String,
        val value: String,
        val data: String
    )
}
