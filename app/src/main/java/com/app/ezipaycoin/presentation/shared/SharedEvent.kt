package com.app.ezipaycoin.presentation.shared

import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse

sealed class SharedEvent {
    data class OpenUrl(val url: String) : SharedEvent()
    data object FetchBalance : SharedEvent()

    /** Screen came back to the foreground: refresh unless data is fresh. */
    data object RefreshIfStale : SharedEvent()

    /** Pull-to-refresh: always refresh, keep showing current data meanwhile. */
    data object PullRefresh : SharedEvent()
    data class CryptoOptionChanged(val crypto: DashboardResponse.Crypto) : SharedEvent()
    data class TransactionSelected(val transaction: TransactionsResponse.TransactionsItem) : SharedEvent()

    data object AppUnlocked : SharedEvent()

    /** The app was in the background long enough: ask for the password again. */
    data object AppLocked : SharedEvent()
}