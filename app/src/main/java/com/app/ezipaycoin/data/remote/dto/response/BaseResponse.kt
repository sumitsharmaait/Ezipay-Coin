package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val apiStatus: Boolean,
    val apiMessage: String,
    val error: String,
    val apiData: T
)
