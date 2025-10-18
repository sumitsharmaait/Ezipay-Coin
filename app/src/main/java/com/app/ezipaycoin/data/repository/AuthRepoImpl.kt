package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.data.remote.dto.request.UpdateProfileRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.LoginResponse
import com.app.ezipaycoin.data.remote.dto.response.NonceResponse
import com.app.ezipaycoin.domain.repository.AuthRepository
import okhttp3.MultipartBody

class AuthRepoImpl(private val apiService: ApiService) : AuthRepository {

    override suspend fun getNonce(key: String): BaseResponse<NonceResponse> {
        return apiService.getNonce(key)
    }

    override suspend fun postLogin(request: LoginRequest): BaseResponse<LoginResponse> {
        return apiService.postLogin(request)
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): BaseResponse<String> {
        return apiService.updateProfile(request)
    }

    override suspend fun logout(): BaseResponse<String> {
        return apiService.logout()
    }

    override suspend fun updateImage(file: MultipartBody.Part): BaseResponse<String> {
        return apiService.updateProfile(file)
    }


}