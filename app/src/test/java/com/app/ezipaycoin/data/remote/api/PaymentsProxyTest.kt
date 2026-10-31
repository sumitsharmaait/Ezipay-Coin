package com.app.ezipaycoin.data.remote.api

import com.app.ezipaycoin.data.remote.dto.response.NetworkInfoByTokenResponse
import com.app.ezipaycoin.data.remote.dto.response.WalletInfoDepositeByTokenResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PaymentsProxyTest {

    private fun failure(code: Int, body: String?): PaymentsException = try {
        PaymentsProxy.map(code, body, WalletInfoDepositeByTokenResponse::class.java)
        fail("expected PaymentsException")
        throw IllegalStateException()
    } catch (e: PaymentsException) {
        e
    }

    @Test
    fun vendorSuccessBodyIsDecodedUnchanged() {
        val body = """{"isSuccess":true,"message":"ok","status":200,
            "result":{"WalletAddress":"0xabc","InvoiceNo":"INV1","Message":null,"TransactionId":null,"RstKey":2}}"""
        val res = PaymentsProxy.map(200, body, WalletInfoDepositeByTokenResponse::class.java)
        assertTrue(res.isSuccess)
        assertEquals("0xabc", res.result.WalletAddress)
        assertEquals("INV1", res.result.InvoiceNo)
        assertEquals(2, res.result.RstKey)
    }

    @Test
    fun networkInfoListIsDecoded() {
        val body = """{"isSuccess":true,"message":"ok","status":200,
            "result":[{"NetworkValue":"BEP20","NetworkText":"BNB Smart Chain"}]}"""
        val res = PaymentsProxy.map(200, body, NetworkInfoByTokenResponse::class.java)
        assertEquals("BEP20", res.result[0].NetworkValue)
    }

    @Test
    fun featureDisabledEnvelopeBecomesFriendlyError() {
        val e = failure(
            200,
            """{"apiStatus":false,"apiMessage":"FEATURE_DISABLED","error":null,"apiData":null}"""
        )
        assertEquals("FEATURE_DISABLED", e.code)
        assertEquals("This service is temporarily unavailable", e.message)
    }

    @Test
    fun vendorUnavailableAndTimeoutShowTheSameMessage() {
        for (code in listOf("PAYMENT_VENDOR_UNAVAILABLE", "PAYMENT_VENDOR_TIMEOUT")) {
            val e = failure(502, """{"apiStatus":false,"apiMessage":"$code"}""")
            assertEquals(code, e.code)
            assertEquals(PaymentsProxy.UNAVAILABLE_MESSAGE, e.message)
        }
    }

    @Test
    fun walletMismatchHasOwnMessage() {
        val e = failure(400, """{"apiStatus":false,"apiMessage":"PAYMENT_WALLET_MISMATCH"}""")
        assertEquals("This wallet does not belong to your account", e.message)
    }

    @Test
    fun emptyUnauthorizedBodyIsSessionExpired() {
        val e = failure(401, null)
        assertEquals("UNAUTHORIZED", e.code)
    }

    @Test
    fun rateLimitIsReported() {
        assertEquals("RATE_LIMITED", failure(429, """{"apiMessage":"RATE_LIMITED"}""").code)
        assertEquals("RATE_LIMITED", failure(429, "").code)
    }

    @Test
    fun vendor4xxKeepsVendorMessage() {
        val e = failure(422, """{"isSuccess":false,"message":"Invalid token value","status":422}""")
        assertEquals("Invalid token value", e.message)
    }

    @Test
    fun garbageSuccessBodyIsUnavailableNotACrash() {
        assertEquals("BAD_REPLY", failure(200, "<html>oops</html>").code)
        assertEquals("BAD_REPLY", failure(200, "").code)
    }

    @Test
    fun idempotencyKeysAreUniqueAndPrintableAscii() {
        val a = PaymentsProxy.newIdempotencyKey()
        val b = PaymentsProxy.newIdempotencyKey()
        assertNotEquals(a, b)
        assertTrue(a.length in 1..100)
        assertTrue(a.all { it.code in 0x21..0x7e })
    }
}
