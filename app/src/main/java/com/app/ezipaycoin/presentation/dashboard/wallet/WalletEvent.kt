package com.app.ezipaycoin.presentation.dashboard.wallet

import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse

sealed class WalletEvent{
    data class ExpandCurrencyOptions(val isExpand : Boolean) : WalletEvent()
    data class CurrentOptionChanged(val currency : DashboardResponse.Crypto) : WalletEvent()
}

