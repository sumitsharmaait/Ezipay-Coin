package com.app.ezipaycoin.presentation.receive

sealed class ReceiveEvent {
    data object DismissDialog : ReceiveEvent()
}