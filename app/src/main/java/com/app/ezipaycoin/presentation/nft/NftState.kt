package com.app.ezipaycoin.presentation.nft

import com.app.ezipaycoin.data.remote.dto.response.NftItem

/** State for the NFTs segment inside WalletScreen. */
data class NftState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val items: List<NftItem> = emptyList(),
    val ownerAddress: String = "",
    val error: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty() && error == null
}

/** State for a single NFT detail screen. */
data class NftDetailState(
    val isLoading: Boolean = false,
    val item: NftItem? = null,
    val owner: String? = null,
    val error: String? = null
)
