package com.app.ezipaycoin.data.remote.api

import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.request.DeleteAccountRequest
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
import com.app.ezipaycoin.data.remote.dto.response.LoginResponse
import com.app.ezipaycoin.data.remote.dto.response.NetworkInfoByTokenResponse
import com.app.ezipaycoin.data.remote.dto.response.NonceResponse
import com.app.ezipaycoin.data.remote.dto.response.MeProfileResponse
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
import com.app.ezipaycoin.data.remote.dto.request.AddTokenRequest
import com.app.ezipaycoin.data.remote.dto.request.StartLivenessRequest

import com.app.ezipaycoin.data.remote.dto.response.KycStatusResponse
import com.app.ezipaycoin.data.remote.dto.response.LivenessSessionResponse
import com.app.ezipaycoin.data.remote.dto.response.TokenMetadataResponse
import com.app.ezipaycoin.data.remote.dto.response.VideoJobStatusResponse
import com.app.ezipaycoin.data.remote.dto.response.VideoUploadResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Path

import com.app.ezipaycoin.data.remote.dto.request.MintAllowlistRequest
import com.app.ezipaycoin.data.remote.dto.request.MintRequest
import com.app.ezipaycoin.data.remote.dto.response.NftCollectionResponse
import com.app.ezipaycoin.data.remote.dto.response.NftHoldingsResponse
import com.app.ezipaycoin.data.remote.dto.response.NftMintResponse
import com.app.ezipaycoin.data.remote.dto.response.NftTokenResponse

interface ApiService {

    @GET("Auth/nonce")
    suspend fun getNonce(@Query("publicKey") publicKey: String): BaseResponse<NonceResponse>

    @POST("Auth/login")
    suspend fun postLogin(@Body request: LoginRequest): BaseResponse<LoginResponse>

    /** Deletes the account server-side. The body must be {"confirm":"DELETE"}. */
    @retrofit2.http.HTTP(method = "DELETE", path = "Users/me", hasBody = true)
    suspend fun deleteAccount(
        @retrofit2.http.Header("Idempotency-Key") idempotencyKey: String,
        @Body request: DeleteAccountRequest
    ): BaseResponse<String?>

    @GET("Auth/logout")
    suspend fun logout(): BaseResponse<String>

    @POST("Users/UpdateProfile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): BaseResponse<String>

    @Multipart
    @POST("Users/ProfilePicUpload")
    suspend fun updateProfile(@Part file: MultipartBody.Part): BaseResponse<String>

    @GET("Users/me")
    suspend fun getMe(): BaseResponse<MeProfileResponse>

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

    // Payments proxy on our backend (the vendor credential lives there). Replies are the vendor's
    // JSON or an apiMessage envelope; PaymentsProxy.map decodes both.
    @POST("Payments/NetworkInfobyTokenId")
    suspend fun getNetworkInfoByTokenID(@Body request: NetworkInfoByTokenRequest): Response<ResponseBody>

    @POST("Payments/CryptoPayinDeposittoken")
    suspend fun getWalletInfoDepositeToken(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: WalletInfoDepositeTokenRequest
    ): Response<ResponseBody>

    @POST("Payments/CryptoPayoutTransfertoken")
    suspend fun postWithdrawalTransferPayout(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: WithdrawalTransferPayoutRequest
    ): Response<ResponseBody>

    @POST("Payments/CryptoPayinTransactionStatusCheck")
    suspend fun checkDepositeTransactionStatus(@Body request: StatusDepositeRequest): Response<ResponseBody>

    // ---------------------- KYC ----------------------

    /**
     * Multipart KYC submission. Parts:
     *   - documentType, documentNumber  (text parts)
     *   - frontImage, selfieImage       (required file parts)
     *   - backImage                     (nullable file part for single-sided docs)
     *
     * Mirrors the existing ProfilePicUpload multipart style.
     */
    @Multipart
    @POST("Kyc/submit")
    suspend fun submitKyc(
        @Part("fullName") fullName: RequestBody,
        @Part("dateOfBirth") dateOfBirth: RequestBody,
        @Part("nationality") nationality: RequestBody,
        @Part("addressLine") addressLine: RequestBody,
        @Part("city") city: RequestBody,
        @Part("postalCode") postalCode: RequestBody,
        @Part("country") country: RequestBody,
        @Part("documentType") documentType: RequestBody,
        @Part("documentNumber") documentNumber: RequestBody,
        @Part("consentAccepted") consentAccepted: RequestBody,
        @Part frontImage: MultipartBody.Part,
        @Part selfieImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part?,
        @Part proofOfAddress: MultipartBody.Part?
    ): BaseResponse<KycStatusResponse>

    @GET("Kyc/status")
    suspend fun getKycStatus(): BaseResponse<KycStatusResponse>

    // ------------------- Add Custom Token -------------------

    /** Auto-fetch: resolves on-chain metadata for a contract address (manual fallback in UI if this fails). */
    @GET("Contract/ValidateToken")
    suspend fun validateToken(
        @Query("contractAddress") contractAddress: String,
        @Query("chainId") chainId: Int
    ): BaseResponse<TokenMetadataResponse>

    @POST("Contract/AddToken")
    suspend fun addToken(@Body request: AddTokenRequest): BaseResponse<TokenMetadataResponse>

    @Multipart
    @POST("Kyc/video/upload")
    suspend fun uploadKycVideo(
        @Part video: MultipartBody.Part,
        @Part("kyc_id") kycId: okhttp3.RequestBody
    ): VideoUploadResponse

    @GET("Kyc/video/status/{jobId}")
    suspend fun getVideoJobStatus(@Path("jobId") jobId: String): VideoJobStatusResponse

    @POST("Kyc/liveness/session")
    suspend fun startLivenessSession(
        @Body body: StartLivenessRequest
    ): LivenessSessionResponse

    // ---------- NFT ----------

    @GET("Nft/collection")
    suspend fun getNftCollection(): BaseResponse<NftCollectionResponse>

    @GET("Nft/holdings/{owner}")
    suspend fun getNftHoldings(
        @Path("owner") owner: String
    ): BaseResponse<NftHoldingsResponse>

    @GET("Nft/token/{tokenId}")
    suspend fun getNftToken(
        @Path("tokenId") tokenId: Long
    ): BaseResponse<NftTokenResponse>

    @POST("Nft/mint/public")
    suspend fun mintNftPublic(
        @Body request: MintRequest
    ): BaseResponse<NftMintResponse>

    @POST("Nft/mint/allowlist")
    suspend fun mintNftAllowlist(
        @Body request: MintAllowlistRequest
    ): BaseResponse<NftMintResponse>


    // ---------- App config, Earn, Staking reads, Learn (content branches) ----------

    @GET("Earn/summary")
    suspend fun getEarnSummary(
        @Query("status") status: String,
        @Query("chainId") chainId: Int
    ): BaseResponse<com.app.ezipaycoin.data.remote.dto.response.RewardSummaryResponse>

    @GET("Earn/checkin/status")
    suspend fun getCheckInStatus(
        @Query("month") month: String
    ): BaseResponse<com.app.ezipaycoin.data.remote.dto.response.CheckInStatusResponse>

    /** A GET that records today's check-in; send an Idempotency-Key so a retry cannot double-grant. */
    @GET("Earn/checkin")
    suspend fun checkIn(
        @Query("date") date: String,
        @retrofit2.http.Header("Idempotency-Key") idempotencyKey: String
    ): BaseResponse<com.app.ezipaycoin.data.remote.dto.response.CheckInResponse>

    @GET("Earn/checkin/tasks")
    suspend fun getDailyTasks(
        @Query("date") date: String
    ): BaseResponse<com.app.ezipaycoin.data.remote.dto.response.DailyTasksResponse>

    @POST("Earn/checkin/tasks/{taskId}/complete")
    suspend fun completeTask(
        @Path("taskId") taskId: Int,
        @Body request: com.app.ezipaycoin.data.remote.dto.request.CompleteTaskRequest,
        @retrofit2.http.Header("Idempotency-Key") idempotencyKey: String
    ): BaseResponse<com.app.ezipaycoin.data.remote.dto.response.CompleteTaskResponse>

    @GET("earn/Staking/pools")
    suspend fun getStakingPools(
        @Query("chainId") chainId: Int
    ): BaseResponse<List<com.app.ezipaycoin.data.remote.dto.response.StakingPoolResponse>>

    @GET("learn")
    suspend fun getLearn(): BaseResponse<List<com.app.ezipaycoin.data.remote.dto.response.LearnItemResponse>>

    @GET("App/config")
    suspend fun getAppConfig(): BaseResponse<com.app.ezipaycoin.data.remote.dto.response.AppConfigResponse>

}
