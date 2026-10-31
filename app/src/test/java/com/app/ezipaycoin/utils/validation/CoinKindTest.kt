package com.app.ezipaycoin.utils.validation

import org.junit.Assert.assertEquals
import org.junit.Test

class CoinKindTest {
    private val ezpt = "0x1111111111111111111111111111111111111111"
    private val usdt = "0x2222222222222222222222222222222222222222"
    private val listed = "0x3333333333333333333333333333333333333333"

    private fun resolve(symbol: String, contract: String? = null) = CoinKind.resolve(symbol, contract, ezpt, usdt)

    @Test
    fun bnbIsNativeEvenIfAContractIsListed() {
        assertEquals(CoinKind.Native, resolve("BNB"))
        assertEquals(CoinKind.Native, resolve("bnb", listed))
    }

    @Test
    fun listedContractWins() {
        assertEquals(CoinKind.Token(listed), resolve("EZPT", listed))
        assertEquals(CoinKind.Token(listed), resolve("SOMETOKEN", listed))
    }

    @Test
    fun knownTokensFallBackToConfiguredContracts() {
        assertEquals(CoinKind.Token(ezpt), resolve("ezpt"))
        assertEquals(CoinKind.Token(usdt), resolve("USDT", null))
    }

    @Test
    fun malformedListedContractIsNotTrusted() {
        assertEquals(CoinKind.Token(usdt), resolve("USDT", "0x123"))
        assertEquals(CoinKind.Unsupported, resolve("DOGE", "not-an-address"))
    }

    @Test
    fun unknownCoinWithoutContractIsUnsupported() {
        assertEquals(CoinKind.Unsupported, resolve("DOGE"))
    }
}
