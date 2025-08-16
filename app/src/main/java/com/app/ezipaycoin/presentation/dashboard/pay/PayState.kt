package com.app.ezipaycoin.presentation.dashboard.pay

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.BnbChainResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.utils.ResponseState

data class PayState(
    var amount: String = "",
    var toAddress: String = "",
    var toAddressError: String? = null,
    var amountError: String? = null,
    var selectedTab: PayScreenTab = PayScreenTab.DirectPay,
    var isCurrencyOptionsExpanded: Boolean = false,
    var broadcastTransaction: Boolean = false,
    var uIMessage: String = "",
    var selectedUtility: String = "Mobile Recharge",
    var responseState: ResponseState<BaseResponse<TransactionsResponse>> = ResponseState.Idle,
    var payMoneyResponse: ResponseState<BnbChainResponse> = ResponseState.Idle,
    var isDialogVisible: Boolean = false
    //var transactionCountResponse: ResponseState<String> = ResponseState.Loading,

)
