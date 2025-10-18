package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val user: User
) {
    @Serializable
    data class User(
        val name: String?,
        val email: String?,
        val profilePic: String?,
        val publicKey: String,
        val createdAt: String,
        val kycStatus: String
    )
}
