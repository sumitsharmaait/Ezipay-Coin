package com.app.ezipaycoin.presentation.nft.mint

sealed interface NftMintEvent {
    data object Load : NftMintEvent
    data object IncrementQuantity : NftMintEvent
    data object DecrementQuantity : NftMintEvent
    data object MintClicked : NftMintEvent
    data object BackClicked : NftMintEvent
    data object ErrorDismissed : NftMintEvent
    data object SuccessAcknowledged : NftMintEvent
    data class QuantityChanged(val quantity: Int) : NftMintEvent
    data class ProofLoaded(val proof: List<String>) : NftMintEvent
}

sealed interface NftMintNavEvent {
    data object Back : NftMintNavEvent
    data class ToHoldings(val transactionHash: String?) : NftMintNavEvent
}
