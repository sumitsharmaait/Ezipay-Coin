package com.app.ezipaycoin.presentation.depositviacard

import androidx.compose.ui.text.input.TextFieldValue

sealed class DepositeViaCardEvent {
    data class DismissDialog(val which: Int) : DepositeViaCardEvent()

    data class OnCardNoChanged(val cardNo: TextFieldValue) : DepositeViaCardEvent()
    data class OnExpiryMonthYearChanged(val expiryMonthYear: TextFieldValue) : DepositeViaCardEvent()
    data class OnCvvChanged(val cvv: String) : DepositeViaCardEvent()
    data class OnAccountNameChanged(val name: String) : DepositeViaCardEvent()
    data class OnAmountChanged(val amount: String) : DepositeViaCardEvent()
    data class OnRemarksChanged(val remarks: String) : DepositeViaCardEvent()
    data object OnDepositSubmit : DepositeViaCardEvent()
}