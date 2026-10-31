package com.app.ezipaycoin.data.repository

import android.util.Log
import com.app.ezipaycoin.utils.UserMessages
import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.response.LivenessSessionResponse
import com.app.ezipaycoin.data.remote.dto.response.VideoJobStatusResponse
import com.app.ezipaycoin.data.remote.dto.response.VideoUploadResponse
import com.app.ezipaycoin.data.remote.dto.request.StartLivenessRequest
import com.app.ezipaycoin.domain.repository.KycVideoRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class KycVideoRepoImpl(
    private val api: ApiService
) : KycVideoRepository {

    override suspend fun uploadVideo(filePath: String, kycId: String) =
        safe {
            val file = File(filePath)

            Log.d("KYC", "Exists=${file.exists()}")
            Log.d("KYC", "Path=${file.absolutePath}")

            val part = MultipartBody.Part.createFormData(
                name = "video",
                filename = file.name,
                body = file.asRequestBody("video/mp4".toMediaTypeOrNull())
            )
            val kycBody = kycId.toRequestBody("text/plain".toMediaTypeOrNull())
            api.uploadKycVideo(part, kycBody)
        }

    override suspend fun pollJobStatus(jobId: String) =
        safe { api.getVideoJobStatus(jobId) }

    override suspend fun startLivenessSession(deviceModel: String) =
        safe { api.startLivenessSession(StartLivenessRequest(deviceModel = deviceModel)) }

    private suspend fun <T> safe(block: suspend () -> T): ResponseState<T> =
        withContext(Dispatchers.IO) {
            try {
                ResponseState.Success(block())
            } catch (e: Exception) {
                ResponseState.Error(UserMessages.of(e, "Something went wrong"))
            }
        }
}
