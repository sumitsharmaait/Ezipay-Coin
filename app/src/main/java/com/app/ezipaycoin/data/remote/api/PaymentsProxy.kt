package com.app.ezipaycoin.data.remote.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.UUID

/** Thrown for any failure of a payments proxy call. The message is safe to show to the user. */
class PaymentsException(val code: String, message: String) : Exception(message)

/**
 * Maps replies of the backend payments proxy (the Payments routes) to the vendor-shaped response
 * classes the screens already use, and turns proxy errors into [PaymentsException].
 */
object PaymentsProxy {

    const val FEATURE_DISABLED = "FEATURE_DISABLED"
    const val UNAVAILABLE_MESSAGE = "This service is temporarily unavailable"

    private val gson = Gson()

    /** One new key per user action. Retries of the same action must reuse it. */
    fun newIdempotencyKey(): String = UUID.randomUUID().toString()

    fun <T> map(httpCode: Int, body: String?, type: Class<T>): T {
        val json = parseObject(body)
        // Our own envelope: { apiStatus, apiMessage, ... }. Vendor replies never carry apiMessage.
        val apiMessage = json?.get("apiMessage")?.takeIf { it.isJsonPrimitive }?.asString
        if (json != null && apiMessage != null) {
            throw PaymentsException(apiMessage, friendlyMessage(apiMessage))
        }
        if (httpCode == 401) {
            throw PaymentsException("UNAUTHORIZED", "Your session has expired. Please sign in again.")
        }
        if (httpCode == 429) {
            throw PaymentsException("RATE_LIMITED", "Too many requests. Please try again in a minute.")
        }
        if (httpCode !in 200..299) {
            val vendorMessage = json?.get("message")?.takeIf { it.isJsonPrimitive }?.asString
            throw PaymentsException(
                "HTTP_$httpCode",
                vendorMessage ?: if (httpCode >= 500) UNAVAILABLE_MESSAGE else "The request was rejected"
            )
        }
        if (json == null) throw PaymentsException("BAD_REPLY", UNAVAILABLE_MESSAGE)
        return gson.fromJson(json, type) ?: throw PaymentsException("BAD_REPLY", UNAVAILABLE_MESSAGE)
    }

    fun friendlyMessage(code: String): String = when (code) {
        FEATURE_DISABLED,
        "PAYMENT_VENDOR_UNAVAILABLE",
        "PAYMENT_VENDOR_TIMEOUT" -> UNAVAILABLE_MESSAGE
        "RATE_LIMITED" -> "Too many requests. Please try again in a minute."
        "PAYMENT_WALLET_MISMATCH" -> "This wallet does not belong to your account"
        "PAYMENT_WALLET_REQUIRED",
        "PAYMENT_BODY_INVALID",
        "PAYMENT_FIELD_NOT_ALLOWED",
        "PAYMENT_REQUEST_REJECTED" -> "The request could not be processed"
        else -> UNAVAILABLE_MESSAGE
    }

    private fun parseObject(body: String?): JsonObject? {
        if (body.isNullOrBlank()) return null
        return try {
            JsonParser.parseString(body).takeIf { it.isJsonObject }?.asJsonObject
        } catch (e: Exception) {
            null
        }
    }
}
