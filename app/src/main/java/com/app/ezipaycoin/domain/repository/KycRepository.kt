package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.request.KycSubmitRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.KycStatusResponse

interface KycRepository {
    suspend fun submitKyc(request: KycSubmitRequest): BaseResponse<KycStatusResponse>
    suspend fun getKycStatus(): BaseResponse<KycStatusResponse>
}
