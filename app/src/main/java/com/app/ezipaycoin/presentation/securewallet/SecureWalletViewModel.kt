package com.app.ezipaycoin.presentation.securewallet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SecureWalletViewModel : ViewModel() {

    var state by mutableStateOf(SecureWalletState())

    init {

    }

    fun onEvent(event: SecureWalletEvent) {
        state = when (event) {
            is SecureWalletEvent.SeedPhraseBottomSheet -> {
                state.copy(seedPhraseBottomSheetVisible = true)
            }

            is SecureWalletEvent.SecureWalletBottomSheet -> {
                state.copy(secureWalletBottomSheetVisible = true)
            }

            is SecureWalletEvent.DismissSecureWalletBottomSheet -> {
                state.copy(secureWalletBottomSheetVisible = false)
            }

            is SecureWalletEvent.DismissSeedPhraseBottomSheet -> {
                state.copy(seedPhraseBottomSheetVisible = false)
            }
        }

    }

}