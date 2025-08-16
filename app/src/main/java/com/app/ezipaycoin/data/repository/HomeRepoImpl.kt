package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.domain.repository.HomeRepository

class HomeRepoImpl(private val apiService: ApiService) : HomeRepository {

    override suspend fun getDashboard(): BaseResponse<DashboardResponse> {
        return apiService.getDashboard()
    }
}