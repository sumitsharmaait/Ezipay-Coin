package com.app.ezipaycoin.presentation.dashboard.pay

import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse

sealed class PayEvent{
    data class PayAmount(val crypto: String) : PayEvent()
    data class AmountChanged(val amount: String) : PayEvent()
    data class ExpandCurrencyOptions(val isExpand : Boolean) : PayEvent()
    data class CurrentOptionChanged(val currency : DashboardResponse.Crypto) : PayEvent()
    data class ToAddressChanged(val toAddress: String) : PayEvent()
    data class TabChanged(val tab: PayScreenTab) : PayEvent()
    data class UtilityItemChanged(val utilityItem: String) : PayEvent()
    data object DismissDialog : PayEvent()
}

