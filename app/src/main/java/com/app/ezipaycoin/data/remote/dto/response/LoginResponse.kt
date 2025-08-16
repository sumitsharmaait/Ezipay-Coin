package com.app.ezipaycoin.data.remote.dto.response

data class LoginResponse(
    val token: String,
    val user: User
) {
    data class User(
        val name: String?,
        val email: String?,
        val profilePic: String?,
        val publicKey: String,
        val createdAt: String,
        val kycStatus: String
    )
}
