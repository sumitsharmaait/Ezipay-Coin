package com.app.ezipaycoin.presentation.withdraw

import com.app.ezipaycoin.data.remote.dto.response.NetworkInfoByTokenResponse
import com.app.ezipaycoin.utils.ResponseState

data class WithdrawState(
    var transferInfoResponse: ResponseState<String> = ResponseState.Idle,
    var networkInfoResponse: ResponseState<List<NetworkInfoByTokenResponse.NetworkInfo>> = ResponseState.Idle,
    var networkInfo: String = "",
    var toAddress: String = "",
    var amount: String = "",
    var amountError : String? = null,
    var toAddressError : String? = null,
    var isScanning: Boolean = false,
    var statusDepositeResponse: ResponseState<Int> = ResponseState.Idle
)
