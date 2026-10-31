package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.response.NftCollectionResponse
import com.app.ezipaycoin.data.remote.dto.response.NftHoldingsResponse
import com.app.ezipaycoin.data.remote.dto.response.NftMintResponse
import com.app.ezipaycoin.data.remote.dto.response.NftTokenResponse
import com.app.ezipaycoin.utils.ResponseState

interface NftRepository {

    /** Collection metadata: supply, prices, and the active sale phase. */
    suspend fun getCollection(): ResponseState<NftCollectionResponse>

    /**
     * NFTs held by [owner]. The address must be passed explicitly — this endpoint
     * does not read it from the JWT.
     */
    suspend fun getHoldings(owner: String): ResponseState<NftHoldingsResponse>

    suspend fun getToken(tokenId: Long): ResponseState<NftTokenResponse>

    suspend fun mintPublic(quantity: Int): ResponseState<NftMintResponse>

    suspend fun mintAllowlist(quantity: Int, proof: List<String>): ResponseState<NftMintResponse>
}
