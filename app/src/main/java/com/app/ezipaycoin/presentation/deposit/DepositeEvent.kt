package com.app.ezipaycoin.presentation.deposit

sealed class DepositeEvent {
    data class DismissDialog(val which: Int) : DepositeEvent()
}