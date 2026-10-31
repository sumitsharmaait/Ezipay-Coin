package com.app.ezipaycoin.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * GET /api/v1/Nft/collection
 *
 * Field names are inferred from EzipayNftDefinitions.cs / NftDTOs.cs on the backend.
 * Verify against a live response and adjust the @SerializedName values if they differ;
 * every field is nullable so a mismatch degrades rather than crashes.
 */
data class NftCollectionResponse(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("symbol")
    val symbol: String? = null,

    @SerializedName("contractAddress")
    val contractAddress: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("totalMinted")
    val totalMinted: Long? = null,

    @SerializedName("maxSupply")
    val maxSupply: Long? = null,

    /** Price in wei as a decimal string. Do not parse into a Double. */
    @SerializedName("publicPrice")
    val publicPriceWei: String? = null,

    /** Price in wei as a decimal string. */
    @SerializedName("allowlistPrice")
    val allowlistPriceWei: String? = null,

    @SerializedName("maxPerWallet")
    val maxPerWallet: Int? = null,

    /** Expected values: "closed", "allowlist", "public". */
    @SerializedName("salePhase")
    val salePhase: String? = null
)
