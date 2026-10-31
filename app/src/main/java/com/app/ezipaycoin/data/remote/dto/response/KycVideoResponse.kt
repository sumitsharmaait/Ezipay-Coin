// data/remote/dto/response/KycVideoResponse.kt
package com.app.ezipaycoin.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// ---- Recorded video upload ----
data class VideoUploadResponse(
    @SerializedName("job_id") val jobId: String,
    @SerializedName("status") val status: String          // QUEUED | PROCESSING | APPROVED | REJECTED
)

data class VideoJobStatusResponse(
    @SerializedName("job_id") val jobId: String,
    @SerializedName("status") val status: String,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("confidence") val confidence: Double? = null
)

// ---- Live session bootstrap (REST) ----
data class LivenessSessionResponse(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("ws_url") val wsUrl: String,
    @SerializedName("token") val token: String
)
