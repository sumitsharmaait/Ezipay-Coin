package com.app.ezipaycoin.data.remote.dto.request

data class StartLivenessRequest(
    val sessionType: String = "ACTIVE_CHALLENGE",
    val deviceModel: String
)
