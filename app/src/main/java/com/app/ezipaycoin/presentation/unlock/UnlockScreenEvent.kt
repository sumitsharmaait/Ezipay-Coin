package com.app.ezipaycoin.presentation.unlock

sealed class UnlockScreenEvent {

    data object OnSubmit : UnlockScreenEvent()

    data class OnPasswordChange(var password: String) : UnlockScreenEvent()

    data object PasswordToggle : UnlockScreenEvent()

    data object OnBioAuthSuccess : UnlockScreenEvent()

}