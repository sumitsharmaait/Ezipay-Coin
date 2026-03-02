package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class DepositeByCardResponse(
    val isSuccess: Boolean,
    val message: String,
    val status: Int,
    val result: ResultData
) {
    @Serializable
    data class ResultData(
        val Url: String?,
        val Message: String?,
        val Invoicenumber: String?,
        val RstKey: Int
    )
}

