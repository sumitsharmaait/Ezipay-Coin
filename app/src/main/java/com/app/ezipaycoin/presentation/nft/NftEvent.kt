package com.app.ezipaycoin.presentation.nft

/** User intents for the NFTs segment. */
sealed interface NftEvent {
    data object Load : NftEvent
    data object Refresh : NftEvent
    data object MintClicked : NftEvent
    data object ErrorDismissed : NftEvent
    data class NftClicked(val tokenId: Long) : NftEvent
}

/** User intents for the NFT detail screen. */
sealed interface NftDetailEvent {
    data class Load(val tokenId: Long) : NftDetailEvent
    data object BackClicked : NftDetailEvent
    data object ErrorDismissed : NftDetailEvent
}

/**
 * One-shot navigation effects. Collected with ObserveAsEvents so they survive
 * recomposition without re-firing.
 */
sealed interface NftNavEvent {
    data object ToMint : NftNavEvent
    data object Back : NftNavEvent
    data class ToDetail(val tokenId: Long) : NftNavEvent
}
