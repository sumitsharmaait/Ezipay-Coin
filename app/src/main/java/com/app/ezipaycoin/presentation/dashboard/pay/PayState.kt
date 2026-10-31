package com.app.ezipaycoin.presentation.dashboard.pay

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
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
    var payMoneyResponse: ResponseState<String> = ResponseState.Idle,
    var isDialogVisible: Boolean = false,
    var isScanning: Boolean = false,
    /** Set once fee and funds are checked; the user must confirm before anything is signed. */
    var confirmation: PayConfirmation? = null,
    /** Hash of a broadcast payment whose receipt has not arrived; blocks sending again. */
    var pendingTxHash: String? = null
    //var transactionCountResponse: ResponseState<String> = ResponseState.Loading,

)

data class PayConfirmation(
    val to: String,
    val amount: String,
    val symbol: String,
    val feeBnb: String
)
