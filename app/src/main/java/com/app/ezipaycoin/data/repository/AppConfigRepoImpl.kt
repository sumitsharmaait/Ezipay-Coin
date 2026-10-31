package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.response.AppConfigResponse
import com.app.ezipaycoin.domain.repository.AppConfigRepository
import com.app.ezipaycoin.utils.ResponseState

class AppConfigRepoImpl(private val apiService: ApiService) : AppConfigRepository {
    override suspend fun getAppConfig(): ResponseState<AppConfigResponse> =
        envelopeCall { apiService.getAppConfig() }
}
