package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.LoginResponse
import com.app.ezipaycoin.data.remote.dto.response.NonceResponse

interface AuthRepository {

    suspend fun getNonce(key: String): BaseResponse<NonceResponse>

    suspend fun postLogin(request: LoginRequest): BaseResponse<LoginResponse>

}