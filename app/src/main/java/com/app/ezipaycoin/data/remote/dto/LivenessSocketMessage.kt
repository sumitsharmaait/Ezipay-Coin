package com.app.ezipaycoin.data.remote.dto

import com.google.gson.annotations.SerializedName

// Server -> client
data class LivenessServerMessage(
    @SerializedName("type") val type: String,            // CHALLENGE | PROGRESS | RESULT | ERROR
    @SerializedName("challenge") val challenge: String? = null, // BLINK | TURN_LEFT | TURN_RIGHT | SMILE
    @SerializedName("step") val step: Int? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("passed") val passed: Boolean? = null,
    @SerializedName("message") val message: String? = null
)

// client -> server
data class LivenessClientMessage(
    @SerializedName("type") val type: String,            // FRAME | CHALLENGE_DONE | ABORT
    @SerializedName("challenge") val challenge: String? = null,
    @SerializedName("frame") val frameBase64: String? = null
)
