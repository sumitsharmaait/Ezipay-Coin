package com.app.ezipaycoin.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * Body for POST /api/v1/Nft/mint/public
 *
 * Total payment is computed server-side as (collection public price * quantity),
 * so the client never sends a price.
 */
data class MintRequest(
    @SerializedName("quantity")
    val quantity: Int
)
