package com.app.ezipaycoin.presentation.transactions

sealed class TransactionsEvent {
    data class FilterChanged(val filter: String) : TransactionsEvent()
}