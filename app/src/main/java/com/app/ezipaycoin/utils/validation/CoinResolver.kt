package com.app.ezipaycoin.utils.validation

/** Decides how a coin selected on the send screen is transferred. */
sealed class CoinKind {
    data object Native : CoinKind()
    data class Token(val contract: String) : CoinKind()
    data object Unsupported : CoinKind()

    companion object {
        private val address = Regex("^0x[0-9a-fA-F]{40}$")

        /**
         * BNB is the native coin. Any other coin uses the contract the dashboard lists for it;
         * when the dashboard lists none, only the two known tokens fall back to the configured
         * contracts. Anything else is refused rather than sent to a guessed contract.
         */
        fun resolve(symbol: String, contractAddress: String?, ezptContract: String, usdtContract: String): CoinKind {
            if (symbol.equals("BNB", ignoreCase = true)) return Native
            val listed = contractAddress?.trim()
            if (listed != null && address.matches(listed)) return Token(listed)
            return when {
                symbol.equals("EZPT", ignoreCase = true) -> Token(ezptContract)
                symbol.equals("USDT", ignoreCase = true) -> Token(usdtContract)
                else -> Unsupported
            }
        }
    }
}
