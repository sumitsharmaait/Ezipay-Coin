package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.request.AddTokenRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.TokenMetadataResponse

interface TokenRepository {
    suspend fun validateToken(contractAddress: String, chainId: Int): BaseResponse<TokenMetadataResponse>
    suspend fun addToken(request: AddTokenRequest): BaseResponse<TokenMetadataResponse>
}
