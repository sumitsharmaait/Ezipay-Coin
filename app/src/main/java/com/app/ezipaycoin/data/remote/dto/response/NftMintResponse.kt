package com.app.ezipaycoin.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * POST /api/v1/Nft/mint/public and /api/v1/Nft/mint/allowlist
 *
 * NOTE: this file was not in the scaffold list — add it alongside the other NFT
 * responses. The mint endpoints submit a transaction, so the useful part of the
 * response is the hash. [tokenIds] is only populated if the backend waits for the
 * receipt and decodes the Transfer logs.
 */
data class NftMintResponse(
    @SerializedName("transactionHash")
    val transactionHash: String? = null,

    @SerializedName("tokenIds")
    val tokenIds: List<Long>? = null,

    @SerializedName("quantity")
    val quantity: Int? = null
)
