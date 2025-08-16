package com.app.ezipaycoin.data.remote.api

import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.request.EstimateGasRequest
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.BnbChainResponse
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.data.remote.dto.response.LoginResponse
import com.app.ezipaycoin.data.remote.dto.response.NonceResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("Auth/nonce")
    suspend fun getNonce(@Query("publicKey") publicKey: String): BaseResponse<NonceResponse>

    @POST("Auth/login")
    suspend fun postLogin(@Body request: LoginRequest): BaseResponse<LoginResponse>

    @GET("dashboard")
    suspend fun getDashboard(): BaseResponse<DashboardResponse>

    @POST("Wallet/Transactions")
    suspend fun postTransactions(@Body request: TransactionsRequest): BaseResponse<TransactionsResponse>

    @POST("/")
    suspend fun payMoney(@Body request: BitPayRequest): BnbChainResponse

    @POST("/")
    suspend fun getTransactionCount(@Body request: BitPayRequest): BnbChainResponse

    @POST("/")
    suspend fun getEstimateGas(@Body request: EstimateGasRequest): BnbChainResponse

}