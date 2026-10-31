package com.app.ezipaycoin.data.remote

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.testutil.Fixtures
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionDisplayTest {
    @Test
    fun `amount label carries sign and symbol`() {
        assertEquals("-1.5000 EZPT", Fixtures.txItem(type = "send").amountLabel)
        assertEquals("+0.0020 BNB", Fixtures.txItem(type = "receive", symbol = "BNB", amount = 0.002).amountLabel)
    }

    @Test
    fun `amount label survives a missing symbol`() {
        assertEquals("+1.5000", Fixtures.txItem(type = "receive").copy(symbol = null).amountLabel)
    }

    @Test
    fun `zero or absent fiat value is hidden`() {
        assertNull(Fixtures.txItem().copy(fiatValue = "0").fiatLabel)
        assertNull(Fixtures.txItem().copy(fiatValue = "0.00").fiatLabel)
        assertNull(Fixtures.txItem().copy(fiatValue = null).fiatLabel)
        assertEquals("$0.10", Fixtures.txItem().copy(fiatValue = "0.10").fiatLabel)
    }

    @Test
    fun `dashboard crypto with null logo and symbol parses`() {
        val json = """{"crypto":[{"tokenId":7,"tokenLogo":null,"contractAddress":null,"symbol":null,"balance":1.5,"fiatValue":0}],"fiat":[],"marketSnapshot":[]}"""
        val c = Gson().fromJson(json, DashboardResponse::class.java).crypto.single()
        assertNull(c.tokenLogo)
        assertNull(c.symbol)
        assertEquals("1.5000", c.formattedBalance)
    }

    @Test
    fun `transaction row with null fields does not throw`() {
        val json = """{"items":[{"id":"x","amount":1.0,"type":null,"symbol":null,"to":null,"from":null,"timestamp":null,"status":null,"fiatValue":null}],"page":1,"size":1,"total":1}"""
        val t = Gson().fromJson(json, TransactionsResponse::class.java).items.single()
        assertEquals("+1.0000", t.amountLabel)
        assertEquals("--", t.formattedDate)
    }
}
