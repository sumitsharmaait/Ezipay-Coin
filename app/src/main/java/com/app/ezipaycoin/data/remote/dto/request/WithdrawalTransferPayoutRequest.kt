package com.app.ezipaycoin.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class WithdrawalTransferPayoutRequest(
    val PrivatekeyofFromwalletaddress: String = "",
    val Fromwalletaddress: String = "",
    val MerchantCallbackUrl: String = "Ezcoin",
    val InvoiceNumber: String = "",
    val TokenValue: String = "",
    val NetworkValue: String = "",
    val Towalletaddress: String = "",
    val Amount: String = "",
    val Contractaddress: String
)
