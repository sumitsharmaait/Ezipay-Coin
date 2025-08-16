package com.app.ezipaycoin.presentation.confirmseedphrase

sealed class ConfirmSeedPhraseEvent {
    data class OnVerifySeedClick(val selectedSeed : String) : ConfirmSeedPhraseEvent()
}