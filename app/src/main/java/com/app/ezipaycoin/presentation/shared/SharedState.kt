package com.app.ezipaycoin.presentation.shared

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.utils.ResponseState

data class SharedState(
    var responseState: ResponseState<BaseResponse<DashboardResponse>> = ResponseState.Idle,
    var selectedCrypto: DashboardResponse.Crypto? = null,
    var dashboardResponse: DashboardResponse? = null
    )
