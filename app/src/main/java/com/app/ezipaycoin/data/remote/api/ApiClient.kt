package com.app.ezipaycoin.data.remote.api

import com.app.ezipaycoin.utils.SessionManager
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://api.ezipaycoin.com/api/v1/"
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Or HEADERS, BASIC, NONE
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor())
        .addInterceptor(logging)
        .build()

    private val client1 = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    //    https://bsc-dataseed.bnbchain.org // main net
   //    https://bsc-testnet-dataseed.bnbchain.org test net
    val retrofitPay: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://bsc-dataseed.bnbchain.org")
            .client(client1)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val clientBasicAuth = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(BasicAuthInterceptor())
        .addInterceptor(logging)
        .build()

    val depositWithdrawalPay: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://apiusdt.iripash.com/api/Payment/")
            .client(clientBasicAuth)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            SessionManager.token?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            return chain.proceed(request)
        }
    }

    class BasicAuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val credentials =
                Credentials.basic("8138", "r0f4z10kkF+xFhaxG6TwjbR5k8s4O6SK0o+jSakusM0=")
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", credentials)
                .build()

            return chain.proceed(newRequest)
        }

    }

}