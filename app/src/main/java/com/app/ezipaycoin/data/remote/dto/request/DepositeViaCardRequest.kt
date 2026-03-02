package com.app.ezipaycoin.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class DepositeViaCardRequest(
    val Remarks: String,
    val InvoiceNumber: String,
    val BankAccountName: String,
    val Amount: String = "",
    val CardNo: String,
    val ExpiryMonth: Int,
    val ExpiryYear: Int,
    val CVV: String,
    val MerchantCallbackUrl: String = "EzipayCoin"
)
