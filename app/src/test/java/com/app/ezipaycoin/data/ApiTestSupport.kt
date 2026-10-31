package com.app.ezipaycoin.data

import com.app.ezipaycoin.data.remote.api.ApiClient
import com.app.ezipaycoin.data.remote.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** A real ApiService (same Gson converter and auth interceptor as production) talking to a MockWebServer. */
class ApiHarness(basePath: String = "/api/v1/") {
    val server = MockWebServer().also { it.start() }

    val api: ApiService = Retrofit.Builder()
        .baseUrl(server.url(basePath))
        .client(OkHttpClient.Builder().addInterceptor(ApiClient.AuthInterceptor()).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    /** Same service, but pointed at the server root, as the chain RPC Retrofit is. */
    val rpc: ApiService = Retrofit.Builder()
        .baseUrl(server.url("/"))
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    fun enqueueJson(body: String, code: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json; charset=utf-8").setBody(body))
    }

    fun enqueueGolden(name: String, code: Int = 200) = enqueueJson(golden(name), code)

    fun shutdown() = server.shutdown()

    companion object {
        fun golden(name: String): String =
            checkNotNull(ApiHarness::class.java.getResource("/golden/$name")) { "missing golden $name" }.readText()
    }
}
