package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.MeProfileResponse

interface UserProfileRepository {
    suspend fun getMe(): BaseResponse<MeProfileResponse>
}
