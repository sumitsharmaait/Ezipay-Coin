package com.app.ezipaycoin.presentation.depositviacard

import androidx.compose.ui.text.input.TextFieldValue
import com.app.ezipaycoin.utils.ResponseState

data class DepositeViaCardState(
    var walletInfoResponse: ResponseState<String> = ResponseState.Idle,
    var networkInfo: String = "",
    var statusDepositeResponse: ResponseState<Int> = ResponseState.Idle,
    var depositeResponse : ResponseState<String> = ResponseState.Idle,
    var walletAddress: String = "",

    var cardNo: TextFieldValue = TextFieldValue(""),
    var cardNoError: String = "",

    var expiryMonthYear: TextFieldValue = TextFieldValue(""),
    var expiryMonthYearError: String = "",

    var cvv: String = "",
    var cvvError: String = "",

    var accountName: String = "",
    var accountNameError: String = "",

    var amount: String = "",
    var amountError: String = "",

    var remarks: String = "",
    var remarksError: String = "",
)
