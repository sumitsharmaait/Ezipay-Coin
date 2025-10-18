package com.app.ezipaycoin.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class WalletInfoDepositeTokenRequest(
    val UserEmailid: String = "Test@co.in",
    val ProjectName: String = "Ezcoin",
    val MerchantCallbackUrl: String = "Ezcoin",
    val InvoiceNumber: String = "",
    val TokenValue: String = "",
    val NetworkValue: String = ""
)