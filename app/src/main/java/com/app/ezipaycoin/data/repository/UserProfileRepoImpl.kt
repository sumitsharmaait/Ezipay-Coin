package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.MeProfileResponse
import com.app.ezipaycoin.domain.repository.UserProfileRepository

class UserProfileRepoImpl(private val apiService: ApiService) : UserProfileRepository {
    override suspend fun getMe(): BaseResponse<MeProfileResponse> = apiService.getMe()
}
