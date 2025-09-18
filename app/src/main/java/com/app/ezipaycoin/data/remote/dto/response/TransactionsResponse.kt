package com.app.ezipaycoin.data.remote.dto.response

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TransactionsResponse(
    val items: List<TransactionsItem>,
    val page: Int,
    val size: Int,
    val total: Int
) {
    data class TransactionsItem(
        val id: String,
        val chain: String,
        val type: String,
        val symbol: String,
        val amount: Double,
        val fiatValue: String,
        val timestamp: String,
        val status: String,
        val to: String,
        val from: String
    ) {
        val formattedAmount: String
            get() = String.format(Locale.US, "%.4f", amount)

        val formattedDate: String
            get() {
                return try {
                    val inputFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
                    val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy | hh:mma", Locale.US)
                    val dateTime = ZonedDateTime.parse(timestamp, inputFormatter)
                    dateTime.format(outputFormatter)
                } catch (e: Exception) {
                    "--"
                }
            }
    }
}
