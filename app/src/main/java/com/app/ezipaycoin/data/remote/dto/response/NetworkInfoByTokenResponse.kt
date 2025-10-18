package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class NetworkInfoByTokenResponse(
    val isSuccess: Boolean,
    val message: String,
    val status: Int,
    val result: List<NetworkInfo>
) {
    @Serializable
    data class NetworkInfo(
        val NetworkValue: String,
        val NetworkText: String
    )
}
