package com.app.ezipaycoin.presentation.seedphraseview

sealed class SeedPhraseEvent {
    data object viewPhrases : SeedPhraseEvent()
}