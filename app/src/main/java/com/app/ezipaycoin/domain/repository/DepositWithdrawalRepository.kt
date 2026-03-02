package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.request.DepositeViaCardRequest
import com.app.ezipaycoin.data.remote.dto.request.NetworkInfoByTokenRequest
import com.app.ezipaycoin.data.remote.dto.request.StatusDepositeRequest
import com.app.ezipaycoin.data.remote.dto.request.WalletInfoDepositeTokenRequest
import com.app.ezipaycoin.data.remote.dto.request.WithdrawalTransferPayoutRequest
import com.app.ezipaycoin.data.remote.dto.response.DepositeByCardResponse
import com.app.ezipaycoin.data.remote.dto.response.NetworkInfoByTokenResponse
import com.app.ezipaycoin.data.remote.dto.response.WalletInfoDepositeByTokenResponse

interface DepositWithdrawalRepository {
    suspend fun getNetworkInfoByToken(request: NetworkInfoByTokenRequest): NetworkInfoByTokenResponse

    suspend fun getWalletInfoDepositeToken(request: WalletInfoDepositeTokenRequest): WalletInfoDepositeByTokenResponse

    suspend fun checkDepositeStatus(request: StatusDepositeRequest): WalletInfoDepositeByTokenResponse

    suspend fun withdrawalPayoutTransfer(request: WithdrawalTransferPayoutRequest): WalletInfoDepositeByTokenResponse

    suspend fun depositeViaCard(request: DepositeViaCardRequest): DepositeByCardResponse

}