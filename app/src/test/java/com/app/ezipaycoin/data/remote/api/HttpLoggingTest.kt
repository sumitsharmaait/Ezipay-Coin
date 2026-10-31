package com.app.ezipaycoin.data.remote.api

import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpLoggingTest {
    @Test
    fun releaseLogsNothing() {
        assertEquals(HttpLoggingInterceptor.Level.NONE, HttpLogging.levelFor(debug = false))
    }

    @Test
    fun debugLogsHeadersOnly() {
        assertEquals(HttpLoggingInterceptor.Level.HEADERS, HttpLogging.levelFor(debug = true))
    }

    @Test
    fun authorizationHeaderIsRedacted() {
        assertTrue(HttpLogging.REDACTED_HEADERS.any { it.equals("Authorization", ignoreCase = true) })
    }
}
