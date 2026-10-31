package com.app.ezipaycoin.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class DeleteAccountRequest(
    val confirm: String = "DELETE"
)
