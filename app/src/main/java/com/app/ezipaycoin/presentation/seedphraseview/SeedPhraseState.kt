package com.app.ezipaycoin.presentation.seedphraseview

data class SeedPhraseState(
    val seedWords: List<String> = emptyList(),
    val isViewed: Boolean = false
)
