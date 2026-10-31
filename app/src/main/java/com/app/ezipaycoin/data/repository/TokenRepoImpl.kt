package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.request.AddTokenRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.TokenMetadataResponse
import com.app.ezipaycoin.domain.repository.TokenRepository

class TokenRepoImpl(private val apiService: ApiService) : TokenRepository {
    override suspend fun validateToken(
        contractAddress: String,
        chainId: Int
    ): BaseResponse<TokenMetadataResponse> {
        return apiService.validateToken(contractAddress, chainId)
    }

    override suspend fun addToken(request: AddTokenRequest): BaseResponse<TokenMetadataResponse> {
        return apiService.addToken(request)
    }
}
