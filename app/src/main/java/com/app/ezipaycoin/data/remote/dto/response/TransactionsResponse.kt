package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data class TransactionsResponse(
    val items: List<TransactionsItem>,
    val page: Int,
    val size: Int,
    val total: Int
) {
    @Serializable
    data class TransactionsItem(
        val id: String,
        val chain: String,
        val type: String?,
        val symbol: String?,
        val amount: Double,
        val fiatValue: String?,
        val timestamp: String?,
        val status: String?,
        val to: String?,
        val from: String?,
        val nonce: String?,
        val gas: String,
        val gasPrice: String,
        val gasUsed: String,
        val cumulativeGasUsed: String
    ) {
        val formattedAmount: String
            get() = String.format(Locale.US, "%.4f", amount)

        val isSend: Boolean
            get() = type.equals("send", ignoreCase = true)

        /** Signed amount with the token symbol, e.g. "+0.0020 BNB". */
        val amountLabel: String
            get() = (if (isSend) "-" else "+") + formattedAmount + symbol?.takeIf { it.isNotBlank() }?.let { " $it" }.orEmpty()

        /** Fiat value for display, or null when the list API sent nothing or zero. */
        val fiatLabel: String?
            get() {
                val v = fiatValue?.trim()?.toBigDecimalOrNull() ?: return null
                return if (v.signum() == 0) null else "$" + v.toPlainString()
            }

        val formattedDate: String
            get() {
                return try {
                    val inputFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
                    val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy | hh:mma", Locale.US)
                    val dateTime = ZonedDateTime.parse(timestamp.orEmpty(), inputFormatter)
                    dateTime.format(outputFormatter)
                } catch (e: Exception) {
                    "--"
                }
            }
    }
}
