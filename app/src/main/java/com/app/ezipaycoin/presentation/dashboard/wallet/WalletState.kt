package com.app.ezipaycoin.presentation.dashboard.wallet

import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse

data class WalletState(
    var isCurrencyOptionsExpanded: Boolean = false,
    var showWithDrawalOrDepositeType: Boolean = false,
    var selectedTokenId: DashboardResponse.Crypto? = null
)
