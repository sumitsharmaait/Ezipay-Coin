package com.app.ezipaycoin.presentation.unlock

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.LoginResponse
import com.app.ezipaycoin.utils.ResponseState

data class UnlockState(
    var password: String = "",
    var passwordError: String = "",
    var passwordVisible: Boolean = false,
    var bioAuthEnabled: Boolean = false,
    var responseState: ResponseState<BaseResponse<LoginResponse>> = ResponseState.Idle,
)
