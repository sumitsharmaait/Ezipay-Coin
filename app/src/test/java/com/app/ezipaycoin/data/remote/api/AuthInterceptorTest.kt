package com.app.ezipaycoin.data.remote.api

import com.app.ezipaycoin.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInterceptorTest {
    private val server = MockWebServer()
    private val saved = SessionManager.token

    @After
    fun tearDown() {
        SessionManager.token = saved
        server.shutdown()
    }

    private fun sentAuthHeader(token: String?): String? {
        server.enqueue(MockResponse().setBody("{}"))
        server.start()
        SessionManager.token = token
        val client = OkHttpClient.Builder().addInterceptor(ApiClient.AuthInterceptor()).build()
        client.newCall(Request.Builder().url(server.url("/api/v1/Auth/nonce")).build()).execute().close()
        return server.takeRequest().getHeader("Authorization")
    }

    @Test
    fun emptyTokenSendsNoAuthorizationHeader() = assertNull(sentAuthHeader(""))

    @Test
    fun blankTokenSendsNoAuthorizationHeader() = assertNull(sentAuthHeader("   "))

    @Test
    fun nullTokenSendsNoAuthorizationHeader() = assertNull(sentAuthHeader(null))

    @Test
    fun realTokenIsSentAsBearer() = assertEquals("Bearer abc.def", sentAuthHeader("abc.def"))
}
