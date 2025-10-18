package com.app.ezipaycoin.presentation.unlock

sealed class UnlockScreenVMEvent {
    data object Unlocked: UnlockScreenVMEvent()
}