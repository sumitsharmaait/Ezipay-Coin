package com.app.ezipaycoin.presentation.deposit

import com.app.ezipaycoin.utils.ResponseState

data class DepositeState(
    var walletInfoResponse: ResponseState<String> = ResponseState.Idle,
    var networkInfo: String = "",
    var statusDepositeResponse: ResponseState<Int> = ResponseState.Idle
)
