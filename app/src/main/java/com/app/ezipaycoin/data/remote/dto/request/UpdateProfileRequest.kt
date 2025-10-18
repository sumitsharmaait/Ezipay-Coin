package com.app.ezipaycoin.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val name: String,
    val email: String,
)
