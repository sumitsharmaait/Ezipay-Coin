package com.app.ezipaycoin.data.remote.dto.response

data class BaseResponse<T>(
    val apiStatus: Boolean,
    val apiMessage: String,
    val error: String,
    val apiData: T
)
