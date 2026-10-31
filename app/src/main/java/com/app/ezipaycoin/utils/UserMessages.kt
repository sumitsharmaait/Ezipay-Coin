package com.app.ezipaycoin.utils

import com.google.gson.JsonParseException

/** Turns a thrown error into text that is safe to show on screen. */
object UserMessages {
    const val UNEXPECTED_RESPONSE = "Unexpected response from server. Please try again."

    fun of(t: Throwable, fallback: String): String =
        if (isParseError(t)) UNEXPECTED_RESPONSE else t.localizedMessage ?: fallback

    fun isParseError(t: Throwable): Boolean {
        var c: Throwable? = t
        var depth = 0
        while (c != null && depth++ < 5) {
            if (c is JsonParseException) return true
            c = c.cause
        }
        return false
    }
}
