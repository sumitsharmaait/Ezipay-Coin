package com.app.ezipaycoin.presentation.dashboard.home

import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse

sealed class HomeEvent {
    data class ExpandCryptoOptions(val isExpand: Boolean) : HomeEvent()
    data class CryptoOptionChanged(val crypto: DashboardResponse.Crypto) : HomeEvent()
    data class ComingSoonDialog(val isVisible: Boolean) : HomeEvent()
}