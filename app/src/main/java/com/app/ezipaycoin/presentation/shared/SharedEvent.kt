package com.app.ezipaycoin.presentation.shared

import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse

sealed class SharedEvent {
    data class OpenUrl(val url: String) : SharedEvent()
    data object FetchBalance : SharedEvent()
    data class CryptoOptionChanged(val crypto: DashboardResponse.Crypto) : SharedEvent()
}