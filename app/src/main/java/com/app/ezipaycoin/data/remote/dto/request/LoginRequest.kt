package com.app.ezipaycoin.data.remote.dto.request

data class LoginRequest(
    val publicKey: String,
    val signature: String,
    val deviceId: String,
)
