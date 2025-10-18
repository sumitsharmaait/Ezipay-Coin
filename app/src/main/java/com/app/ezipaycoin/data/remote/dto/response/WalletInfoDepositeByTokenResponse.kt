package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class WalletInfoDepositeByTokenResponse(
    val isSuccess: Boolean,
    val message: String,
    val status: Int,
    val result: ResultData
) {
    @Serializable
    data class ResultData(
        val WalletAddress: String?,
        val InvoiceNo: String,
        val Message: String?,
        val TransactionId: String?,
        val RstKey: Int
    )
}

