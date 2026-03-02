package com.app.ezipaycoin.data.remote.api

import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.request.DepositeViaCardRequest
import com.app.ezipaycoin.data.remote.dto.request.EstimateGasRequest
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.data.remote.dto.request.NetworkInfoByTokenRequest
import com.app.ezipaycoin.data.remote.dto.request.StatusDepositeRequest
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.data.remote.dto.request.UpdateProfileRequest
import com.app.ezipaycoin.data.remote.dto.request.WalletInfoDepositeTokenRequest
import com.app.ezipaycoin.data.remote.dto.request.WithdrawalTransferPayoutRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.BnbChainResponse
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.data.remote.dto.response.DepositeByCardResponse
import com.app.ezipaycoin.data.remote.dto.response.LoginResponse
import com.app.ezipaycoin.data.remote.dto.response.NetworkInfoByTokenResponse
import com.app.ezipaycoin.data.remote.dto.response.NonceResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionReceiptResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.data.remote.dto.response.WalletInfoDepositeByTokenResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @GET("Auth/nonce")
    suspend fun getNonce(@Query("publicKey") publicKey: String): BaseResponse<NonceResponse>

    @POST("Auth/login")
    suspend fun postLogin(@Body request: LoginRequest): BaseResponse<LoginResponse>

    @GET("Auth/logout")
    suspend fun logout(): BaseResponse<String>

    @POST("Users/UpdateProfile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): BaseResponse<String>

    @Multipart
    @POST("Users/ProfilePicUpload")
    suspend fun updateProfile(@Part file: MultipartBody.Part): BaseResponse<String>

    @GET("dashboard")
    suspend fun getDashboard(): BaseResponse<DashboardResponse>

    @POST("Wallet/Transactions")
    suspend fun postTransactions(@Body request: TransactionsRequest): BaseResponse<TransactionsResponse>

    @POST("/")
    suspend fun payMoney(@Body request: BitPayRequest): BnbChainResponse

    @POST("/")
    suspend fun transactionReceipt(@Body request: BitPayRequest): TransactionReceiptResponse

    @POST("/")
    suspend fun getTransactionCount(@Body request: BitPayRequest): BnbChainResponse

    @POST("/")
    suspend fun getEstimateGas(@Body request: EstimateGasRequest): BnbChainResponse

    @POST("NetworkInfobyTokenId")
    suspend fun getNetworkInfoByTokenID(@Body request: NetworkInfoByTokenRequest): NetworkInfoByTokenResponse

    @POST("CryptoPayinDeposittoken")
    suspend fun getWalletInfoDepositeToken(@Body request: WalletInfoDepositeTokenRequest): WalletInfoDepositeByTokenResponse

    @POST("CryptoPayoutTransfertoken")
    suspend fun postWithdrawalTransferPayout(@Body request: WithdrawalTransferPayoutRequest): WalletInfoDepositeByTokenResponse

    @POST("CryptoPayinTransactionStatusCheck")
    suspend fun checkDepositeTransactionStatus(@Body request: StatusDepositeRequest): WalletInfoDepositeByTokenResponse

    @POST("CardTxn")
    suspend fun postDepositeViaCard(@Body request: DepositeViaCardRequest): DepositeByCardResponse

}