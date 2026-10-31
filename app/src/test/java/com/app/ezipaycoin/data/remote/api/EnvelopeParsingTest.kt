package com.app.ezipaycoin.data.remote.api

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.utils.UserMessages
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class EnvelopeParsingTest {
    private val type = object : TypeToken<BaseResponse<TransactionsResponse>>() {}.type
    private val plain = com.google.gson.Gson()

    private fun parse(json: String): BaseResponse<TransactionsResponse> =
        EnvelopeAdapterFactory.gson.fromJson(json, type)

    @Test
    fun refusedEnvelopeWithTextDataKeepsTheMessage() {
        val json = """{"apiStatus":false,"apiMessage":"Invalid request","error":"E1","apiData":"Wallet not found"}"""
        // the plain model still fails, which is the defect being fixed
        try {
            plain.fromJson<BaseResponse<TransactionsResponse>>(json, type)
            fail("expected JsonSyntaxException")
        } catch (_: JsonSyntaxException) {
        }
        val res = parse(json)
        assertFalse(res.apiStatus)
        assertEquals("Invalid request", res.apiMessage)
        assertEquals("E1", res.error)
        @Suppress("USELESS_CAST")
        assertNull(res.apiData as TransactionsResponse?)
    }

    @Test
    fun refusedEnvelopeWithArrayDataKeepsTheMessage() {
        val res = parse("""{"apiStatus":false,"apiMessage":"nope","error":"","apiData":[1,2]}""")
        assertFalse(res.apiStatus)
        assertEquals("nope", res.apiMessage)
    }

    @Test
    fun successfulEnvelopeParsesExactlyAsBefore() {
        val json = """{"apiStatus":true,"apiMessage":"ok","error":"","apiData":
            {"items":[],"page":2,"size":20,"total":0}}"""
        val a = parse(json)
        val b = plain.fromJson<BaseResponse<TransactionsResponse>>(json, type)
        assertEquals(b, a)
        assertTrue(a.apiStatus)
        assertEquals(2, a.apiData.page)
    }

    @Test
    fun successfulEnvelopeWithBadDataStillFails() {
        try {
            parse("""{"apiStatus":true,"apiMessage":"ok","error":"","apiData":"oops"}""")
            fail("expected JsonSyntaxException")
        } catch (_: JsonSyntaxException) {
        }
    }

    @Test
    fun parseErrorsGetAFriendlyMessage() {
        val e = JsonSyntaxException("Expected BEGIN_OBJECT but was STRING at line 1 column 90")
        assertEquals(UserMessages.UNEXPECTED_RESPONSE, UserMessages.of(e, "Unknown error"))
        assertEquals(UserMessages.UNEXPECTED_RESPONSE, UserMessages.of(RuntimeException(e), "x"))
        assertEquals("boom", UserMessages.of(IllegalStateException("boom"), "x"))
    }
}
