package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.data.remote.dto.request.UpdateProfileRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.LoginResponse
import com.app.ezipaycoin.data.remote.dto.response.NonceResponse
import okhttp3.MultipartBody

interface AuthRepository {

    suspend fun getNonce(key: String): BaseResponse<NonceResponse>

    suspend fun postLogin(request: LoginRequest): BaseResponse<LoginResponse>

    suspend fun updateProfile(request: UpdateProfileRequest): BaseResponse<String>

    suspend fun logout() : BaseResponse<String>

    suspend fun updateImage(file: MultipartBody.Part) : BaseResponse<String>

}