package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.request.EstimateGasRequest
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.BnbChainResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.domain.repository.TransactionsRepository

class TransactionRepoImpl(private val apiService: ApiService, private val bitService: ApiService) :
    TransactionsRepository {

    override suspend fun getTransactions(request: TransactionsRequest): BaseResponse<TransactionsResponse> {
        return apiService.postTransactions(request)
    }

    override suspend fun payMoney(request: BitPayRequest): BnbChainResponse {
        return bitService.payMoney(request)
    }

    override suspend fun getTransactionCount(request: BitPayRequest): BnbChainResponse {
        return bitService.getTransactionCount(request)
    }

    override suspend fun getEstimateGas(request: EstimateGasRequest): BnbChainResponse {
        return bitService.getEstimateGas(request)
    }

}