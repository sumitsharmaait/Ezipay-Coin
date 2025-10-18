package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class DashboardResponse(
    val crypto: List<Crypto>,
    val fiat: List<Fiat>,
    val marketSnapshot: List<MarketSnapshot>
) {
    @Serializable
    data class Crypto(
        val tokenId: Int?,
        val tokenLogo: String,
        val contractAddress: String?,
        val symbol: String,
        val balance: Double,
        val fiatValue: Double
    ) {
        val formattedBalance: String
            get() = String.format(Locale.US, "%.4f", balance)

        val formattedFiatValue: String
            get() = String.format(Locale.US, "%.4f", fiatValue)
    }
    @Serializable
    data class Fiat(
        val currency: String,
        val balance: String
    )
    @Serializable
    data class MarketSnapshot(
        val symbol: String,
        val sparkline: List<Float>,
        val changePct: Double
    )
}
