package com.app.ezipaycoin.utils.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class TxErrorMapperTest {
    @Test
    fun erc20InsufficientBalanceRevert() {
        val raw = "execution reverted: custom error 0xe450d38c:000000000000000000000000abc"
        assertEquals("Insufficient token balance", TxErrorMapper.friendly(raw))
    }

    @Test
    fun gasAndNonceErrors() {
        assertEquals(AmountValidator.NO_FEE_BNB, TxErrorMapper.friendly("insufficient funds for gas * price + value"))
        assertEquals("Another transaction is still pending. Wait a moment and try again", TxErrorMapper.friendly("nonce too low"))
    }

    @Test
    fun networkErrors() {
        assertEquals("The network took too long to respond. Please try again", TxErrorMapper.friendly(SocketTimeoutException("timeout")))
        assertEquals("No connection. Check your internet and try again", TxErrorMapper.friendly(UnknownHostException("x")))
    }

    @Test
    fun unknownTextIsNeverShownRaw() {
        val raw = "-32000 some internal rpc detail 0xdeadbeef"
        assertEquals(TxErrorMapper.GENERIC, TxErrorMapper.friendly(raw))
        assertFalse(TxErrorMapper.friendly(null).contains("rpc"))
        assertEquals(TxErrorMapper.GENERIC, TxErrorMapper.friendly(""))
    }
}

class CoinResolverTest {
    private val ezpt = "0x50387a24253150e808875f3cac8111546623ba64"
    private val usdt = "0x55d398326f99059fF775485246999027B3197955"
    private val other = "0x1111111111111111111111111111111111111111"

    @Test
    fun bnbIsNative() {
        assertEquals(CoinKind.Native, CoinKind.resolve("bnb", null, ezpt, usdt))
    }

    @Test
    fun listedContractWins() {
        assertEquals(CoinKind.Token(other), CoinKind.resolve("CAKE", other, ezpt, usdt))
    }

    @Test
    fun knownSymbolsFallBackToConfiguredContracts() {
        assertEquals(CoinKind.Token(ezpt), CoinKind.resolve("EZPT", null, ezpt, usdt))
        assertEquals(CoinKind.Token(usdt), CoinKind.resolve("USDT", "", ezpt, usdt))
    }

    @Test
    fun unknownCoinWithoutContractIsRefused() {
        assertEquals(CoinKind.Unsupported, CoinKind.resolve("CAKE", null, ezpt, usdt))
        assertEquals(CoinKind.Unsupported, CoinKind.resolve("CAKE", "not-an-address", ezpt, usdt))
    }
}

class ReceiptPollingTest {
    @Test
    fun scheduleSumsToTotalAndBacksOff() {
        val s = ReceiptPolling.schedule(60_000, 2_000, 8_000)
        assertEquals(60_000L, s.sum())
        assertEquals(2_000L, s.first())
        assertEquals(3_000L, s[1])
        assertEquals(true, s.all { it <= 8_000 })
    }
}
