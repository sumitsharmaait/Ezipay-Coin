package com.app.ezipaycoin.data.remote.api

import okhttp3.logging.HttpLoggingInterceptor

object HttpLogging {
    val REDACTED_HEADERS = listOf("Authorization", "Idempotency-Key")

    /** Headers only in debug (bodies carry tokens and card data), nothing in release. */
    fun levelFor(debug: Boolean): HttpLoggingInterceptor.Level =
        if (debug) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
}
