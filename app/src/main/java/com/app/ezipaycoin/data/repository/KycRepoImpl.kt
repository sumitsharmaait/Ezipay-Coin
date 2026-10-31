package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.request.KycSubmitRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.KycStatusResponse
import com.app.ezipaycoin.domain.repository.KycRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class KycRepoImpl(private val apiService: ApiService) : KycRepository {

    override suspend fun submitKyc(request: KycSubmitRequest): BaseResponse<KycStatusResponse> {
        val textType = "text/plain".toMediaTypeOrNull()

        fun textPart(value: String) = value.toRequestBody(textType)

        return apiService.submitKyc(
            fullName = textPart(request.fullName.orEmpty()),
            dateOfBirth = textPart(request.dateOfBirth.orEmpty()),
            nationality = textPart(request.nationality.orEmpty()),
            addressLine = textPart(request.addressLine.orEmpty()),
            city = textPart(request.city.orEmpty()),
            postalCode = textPart(request.postalCode.orEmpty()),
            country = textPart(request.country.orEmpty()),
            documentType = textPart(request.documentType),
            documentNumber = textPart(request.documentNumber),
            consentAccepted = textPart(request.consentAccepted.toString()),
            frontImage = filePart("frontImage", request.frontImagePath),
            selfieImage = filePart("selfieImage", request.selfieImagePath),
            backImage = request.backImagePath?.let { filePart("backImage", it) },
            proofOfAddress = request.proofOfAddressPath?.let { filePart("proofOfAddress", it) }
        )
    }

    override suspend fun getKycStatus(): BaseResponse<KycStatusResponse> {
        return apiService.getKycStatus()
    }

    private fun filePart(name: String, path: String): MultipartBody.Part {
        val file = File(path)
        val body: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, file.name, body)
    }
}