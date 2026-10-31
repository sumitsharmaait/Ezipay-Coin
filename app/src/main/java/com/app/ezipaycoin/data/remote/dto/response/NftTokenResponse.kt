package com.app.ezipaycoin.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * GET /api/v1/Nft/token/{tokenId}
 *
 * Same shape as [NftItem] plus current ownership, which the holdings list omits
 * because ownership is implied there.
 */
data class NftTokenResponse(
    @SerializedName("tokenId")
    val tokenId: Long,

    @SerializedName("owner")
    val owner: String? = null,

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
) {
    fun toItem(): NftItem = NftItem(
        tokenId = tokenId,
        name = name,
        description = description,
        imageUrl = imageUrl,
        tokenUri = tokenUri,
        contractAddress = contractAddress,
        attributes = attributes
    )
}
