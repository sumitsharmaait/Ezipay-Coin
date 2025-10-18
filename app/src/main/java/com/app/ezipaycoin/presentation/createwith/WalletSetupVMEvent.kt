package com.app.ezipaycoin.presentation.createwith

sealed class WalletSetupVMEvent {
    data object MoveToSuccess: WalletSetupVMEvent()
}