package com.app.ezipaycoin.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * GET /api/v1/Nft/holdings/{owner}
 *
 * Unlike /Dashboard and /Wallet/balances, this endpoint takes the wallet address as a
 * path parameter instead of reading it from the JWT claims, so the caller has to
 * supply it explicitly from SessionManager.
 */
data class NftHoldingsResponse(
    @SerializedName("owner")
    val owner: String? = null,

    @SerializedName("count")
    val count: Int? = null,

    @SerializedName("items")
    val items: List<NftItem>? = null
)

/** A single NFT as it appears in a holdings list. */
data class NftItem(
    @SerializedName("tokenId")
    val tokenId: Long,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("tokenUri")
    val tokenUri: String? = null,

    @SerializedName("contractAddress")
    val contractAddress: String? = null,

    @SerializedName("attributes")
    val attributes: List<NftAttribute>? = null
)

data class NftAttribute(
    @SerializedName("traitType")
    val traitType: String? = null,

    @SerializedName("value")
    val value: String? = null
)
