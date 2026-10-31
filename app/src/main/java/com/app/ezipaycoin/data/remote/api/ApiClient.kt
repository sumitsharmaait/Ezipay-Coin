package com.app.ezipaycoin.data.remote.api

import com.app.ezipaycoin.BuildConfig
import com.app.ezipaycoin.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = BuildConfig.API_BASE_URL
    // Headers in debug, nothing in release. The bearer token is never written to the log.
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLogging.levelFor(BuildConfig.DEBUG)
        HttpLogging.REDACTED_HEADERS.forEach { redactHeader(it) }
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor())
        .authenticator(TokenAuthenticator())
        .addInterceptor(logging)
        .build()

    private val client1 = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    /** No authenticator and no stored token: used to sign in again after a 401 without recursing. */
    val authRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client1)
            .addConverterFactory(GsonConverterFactory.create(EnvelopeAdapterFactory.gson))
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(EnvelopeAdapterFactory.gson))
            .build()
    }

    //    https://bsc-dataseed.bnbchain.org // main net
   //    https://bsc-testnet-dataseed.bnbchain.org test net
    val retrofitPay: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.RPC_URL)
            .client(client1)
            .addConverterFactory(GsonConverterFactory.create(EnvelopeAdapterFactory.gson))
            .build()
    }

    class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            // Pre-login calls (nonce, login) have no token yet: send no header at all.
            SessionManager.token?.takeIf { it.isNotBlank() }?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            return chain.proceed(request)
        }
    }

}
