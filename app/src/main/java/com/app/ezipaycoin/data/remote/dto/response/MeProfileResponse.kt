package com.app.ezipaycoin.data.remote.dto.response

/** Body of `GET Users/me` (the `apiData` field). Unknown fields are ignored. */
data class MeProfileResponse(
    val publicKey: String?,
    val name: String?,
    val email: String?,
    val profilePic: String?,
    val kycStatus: String?
)
