package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.request.EstimateGasRequest
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.BnbChainResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionReceiptResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse

interface TransactionsRepository {

    suspend fun getTransactions(request: TransactionsRequest): BaseResponse<TransactionsResponse>

    suspend fun payMoney(request: BitPayRequest): BnbChainResponse

    suspend fun getTransactionReceipt(request: BitPayRequest): TransactionReceiptResponse

    suspend fun getTransactionCount(request: BitPayRequest): BnbChainResponse

    suspend fun getEstimateGas(request: EstimateGasRequest): BnbChainResponse

}