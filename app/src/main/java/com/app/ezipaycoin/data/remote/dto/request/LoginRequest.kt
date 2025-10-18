package com.app.ezipaycoin.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val publicKey: String,
    val signature: String,
    val deviceId: String,
)
