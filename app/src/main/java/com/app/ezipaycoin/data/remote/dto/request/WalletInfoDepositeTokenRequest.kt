package com.app.ezipaycoin.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class WalletInfoDepositeTokenRequest(
    val UserEmailid: String = "support@biznetpe.com",
    val ProjectName: String = "1",
    val MerchantCallbackUrl: String = "www",
    val InvoiceNumber: String = "",
    val TokenValue: String = "",
    val NetworkValue: String = "",
    val RecipientWalletAddressCrypto: String
)