package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.request.DepositeViaCardRequest
import com.app.ezipaycoin.data.remote.dto.request.NetworkInfoByTokenRequest
import com.app.ezipaycoin.data.remote.dto.request.StatusDepositeRequest
import com.app.ezipaycoin.data.remote.dto.request.WalletInfoDepositeTokenRequest
import com.app.ezipaycoin.data.remote.dto.request.WithdrawalTransferPayoutRequest
import com.app.ezipaycoin.data.remote.dto.response.DepositeByCardResponse
import com.app.ezipaycoin.data.remote.dto.response.NetworkInfoByTokenResponse
import com.app.ezipaycoin.data.remote.dto.response.WalletInfoDepositeByTokenResponse
import com.app.ezipaycoin.domain.repository.DepositWithdrawalRepository

class DepositWithdrawalRepoImpl(private val apiService: ApiService) : DepositWithdrawalRepository {
    override suspend fun getNetworkInfoByToken(request: NetworkInfoByTokenRequest): NetworkInfoByTokenResponse {
        return apiService.getNetworkInfoByTokenID(request)
    }

    override suspend fun getWalletInfoDepositeToken(request: WalletInfoDepositeTokenRequest): WalletInfoDepositeByTokenResponse {
        return apiService.getWalletInfoDepositeToken(request)
    }

    override suspend fun checkDepositeStatus(request: StatusDepositeRequest): WalletInfoDepositeByTokenResponse {
        return apiService.checkDepositeTransactionStatus(request)
    }

    override suspend fun withdrawalPayoutTransfer(request: WithdrawalTransferPayoutRequest): WalletInfoDepositeByTokenResponse {
        return apiService.postWithdrawalTransferPayout(request)
    }

    override suspend fun depositeViaCard(request: DepositeViaCardRequest): DepositeByCardResponse {
        return apiService.postDepositeViaCard(request)
    }

}