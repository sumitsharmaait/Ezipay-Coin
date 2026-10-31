package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.response.AppConfigResponse
import com.app.ezipaycoin.utils.ResponseState

interface AppConfigRepository {
    suspend fun getAppConfig(): ResponseState<AppConfigResponse>
}
