package com.app.ezipaycoin.utils.validation

import java.math.BigDecimal

/** Amount and funds checks for the send screen. Money is BigDecimal throughout, never Double. */
object AmountValidator {
    const val MAX_DECIMALS = 18
    const val INVALID = "Enter a valid amount"
    const val TOO_MANY_DECIMALS = "Amount can have at most 18 decimal places"
    const val INSUFFICIENT_BALANCE = "Insufficient balance"
    const val NO_FEE_BNB = "Not enough BNB for network fee"

    private val WEI_PER_COIN = BigDecimal("1000000000000000000")

    /** Parses user input. Returns the amount, or null when it is blank, malformed, zero or negative. */
    fun parse(raw: String): BigDecimal? {
        val text = raw.trim()
        if (text.isEmpty() || !Regex("^[0-9]*\\.?[0-9]+$|^[0-9]+\\.$").matches(text)) return null
        val value = text.toBigDecimalOrNull() ?: return null
        return if (value.signum() > 0) value else null
    }

    /** Syntax-only check (no balance), returns an error message or null. */
    fun validateInput(raw: String): String? {
        if (raw.isBlank()) return "Please enter an amount"
        val value = parse(raw) ?: return INVALID
        if (value.stripTrailingZeros().scale() > MAX_DECIMALS) return TOO_MANY_DECIMALS
        return null
    }

    /**
     * Checks the amount against balances. [feeBnb] is the estimated network fee in BNB, or null
     * when not yet known (then the fee part is skipped).
     */
    fun validateFunds(
        amount: BigDecimal,
        symbol: String,
        tokenBalance: BigDecimal?,
        bnbBalance: BigDecimal?,
        feeBnb: BigDecimal?
    ): String? {
        val isBnb = symbol.equals("BNB", ignoreCase = true)
        if (tokenBalance != null && amount > tokenBalance) return INSUFFICIENT_BALANCE
        if (feeBnb != null && bnbBalance != null) {
            if (isBnb) {
                if (amount + feeBnb > bnbBalance) return "Insufficient balance after the network fee"
            } else if (bnbBalance < feeBnb) {
                return NO_FEE_BNB
            }
        }
        return null
    }

    fun weiToBnb(wei: java.math.BigInteger): BigDecimal = BigDecimal(wei).divide(WEI_PER_COIN)

    fun toWei(amount: BigDecimal): java.math.BigInteger = amount.multiply(WEI_PER_COIN).toBigInteger()
}
