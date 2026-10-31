package com.app.ezipaycoin.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * Body for POST /api/v1/Nft/mint/allowlist
 *
 * [proof] is the Merkle proof authorising this wallet against the root configured
 * on-chain. It is validated on-chain, so an empty or stale proof fails at mint time
 * rather than at request time.
 */
data class MintAllowlistRequest(
    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("proof")
    val proof: List<String>
)
