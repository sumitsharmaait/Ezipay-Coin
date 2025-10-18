package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class NonceResponse(
    val publicKey: String,
    val nonce: String
)
