package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.response.LivenessSessionResponse
import com.app.ezipaycoin.data.remote.dto.response.VideoJobStatusResponse
import com.app.ezipaycoin.data.remote.dto.response.VideoUploadResponse
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.flow.Flow

interface KycVideoRepository {
    suspend fun uploadVideo(filePath: String, kycId: String): ResponseState<VideoUploadResponse>
    suspend fun pollJobStatus(jobId: String): ResponseState<VideoJobStatusResponse>
    suspend fun startLivenessSession(deviceModel: String): ResponseState<LivenessSessionResponse>
}
