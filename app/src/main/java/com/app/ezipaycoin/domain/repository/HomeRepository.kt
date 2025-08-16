package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse

interface HomeRepository {

    suspend fun getDashboard() : BaseResponse<DashboardResponse>

}