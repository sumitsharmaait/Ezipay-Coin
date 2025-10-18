package com.app.ezipaycoin.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class StatusDepositeRequest(
    val ClientReferenceNo: String = ""
)
