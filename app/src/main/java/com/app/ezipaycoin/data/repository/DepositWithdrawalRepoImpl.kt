package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.api.PaymentsProxy
import com.app.ezipaycoin.data.remote.dto.request.NetworkInfoByTokenRequest
import com.app.ezipaycoin.data.remote.dto.request.StatusDepositeRequest
import com.app.ezipaycoin.data.remote.dto.request.WalletInfoDepositeTokenRequest
import com.app.ezipaycoin.data.remote.dto.request.WithdrawalTransferPayoutRequest
import com.app.ezipaycoin.data.remote.dto.response.NetworkInfoByTokenResponse
import com.app.ezipaycoin.data.remote.dto.response.WalletInfoDepositeByTokenResponse
import com.app.ezipaycoin.domain.repository.DepositWithdrawalRepository
import okhttp3.ResponseBody
import retrofit2.Response

class DepositWithdrawalRepoImpl(private val apiService: ApiService) : DepositWithdrawalRepository {

    private fun <T> Response<ResponseBody>.decode(type: Class<T>): T {
        val text = try {
            if (isSuccessful) body()?.string() else errorBody()?.string()
        } catch (e: java.io.IOException) {
            null
        }
        return PaymentsProxy.map(code(), text, type)
    }

    override suspend fun getNetworkInfoByToken(request: NetworkInfoByTokenRequest): NetworkInfoByTokenResponse {
        return apiService.getNetworkInfoByTokenID(request).decode(NetworkInfoByTokenResponse::class.java)
    }

    override suspend fun getWalletInfoDepositeToken(
        request: WalletInfoDepositeTokenRequest,
        idempotencyKey: String
    ): WalletInfoDepositeByTokenResponse {
        return apiService.getWalletInfoDepositeToken(idempotencyKey, request)
            .decode(WalletInfoDepositeByTokenResponse::class.java)
    }

    override suspend fun checkDepositeStatus(request: StatusDepositeRequest): WalletInfoDepositeByTokenResponse {
        return apiService.checkDepositeTransactionStatus(request)
            .decode(WalletInfoDepositeByTokenResponse::class.java)
    }

    override suspend fun withdrawalPayoutTransfer(
        request: WithdrawalTransferPayoutRequest,
        idempotencyKey: String
    ): WalletInfoDepositeByTokenResponse {
        return apiService.postWithdrawalTransferPayout(idempotencyKey, request)
            .decode(WalletInfoDepositeByTokenResponse::class.java)
    }


}
