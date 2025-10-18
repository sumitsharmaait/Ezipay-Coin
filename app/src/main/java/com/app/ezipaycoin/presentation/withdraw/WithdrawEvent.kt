package com.app.ezipaycoin.presentation.withdraw

sealed class WithdrawEvent {
    data class AmountChanged(val amount: String) : WithdrawEvent()
    data class DismissDialog(val which : Int) : WithdrawEvent()
    data object WithdrawAmount : WithdrawEvent()
    data class ToAddressChanged(val toAddress: String) : WithdrawEvent()

    data class QRSuccess(val qr: String) : WithdrawEvent()
    data object QRRetry : WithdrawEvent()
    data object QRDispose : WithdrawEvent()

}