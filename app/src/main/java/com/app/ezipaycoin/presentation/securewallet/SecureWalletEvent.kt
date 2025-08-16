package com.app.ezipaycoin.presentation.securewallet

sealed class SecureWalletEvent{
    data object SeedPhraseBottomSheet : SecureWalletEvent()
    data object SecureWalletBottomSheet : SecureWalletEvent()
    data object DismissSeedPhraseBottomSheet : SecureWalletEvent()
    data object DismissSecureWalletBottomSheet : SecureWalletEvent()
}

