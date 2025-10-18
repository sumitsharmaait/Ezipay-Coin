package com.app.ezipaycoin.presentation.createwith

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.NonceResponse
import com.app.ezipaycoin.utils.ResponseState

data class WalletSetupState(
    var responseState: ResponseState<BaseResponse<NonceResponse>> = ResponseState.Idle,
    var loadingSocialSignIn: Boolean = false
)
