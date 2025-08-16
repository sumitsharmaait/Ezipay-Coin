package com.app.ezipaycoin.data.remote.dto.response

import java.util.Locale

data class DashboardResponse(
    val crypto: List<Crypto>,
    val fiat: List<Fiat>,
    val marketSnapshot: List<MarketSnapshot>
) {
    data class Crypto(
        val symbol: String,
        val balance: Double?,
        val fiatValue: Double?
    ) {
        val formattedBalance: String
            get() = String.format(Locale.US, "%.4f", balance ?: "0.0")

        val formattedFiatValue: String
            get() = String.format(Locale.US, "%.4f", fiatValue ?: "0.0")
    }

    data class Fiat(
        val currency: String,
        val balance: String
    )

    data class MarketSnapshot(
        val symbol: String,
        val sparkline: List<Float>,
        val changePct: Double
    )
}
