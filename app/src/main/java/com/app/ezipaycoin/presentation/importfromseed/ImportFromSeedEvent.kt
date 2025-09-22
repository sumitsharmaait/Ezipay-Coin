package com.app.ezipaycoin.presentation.importfromseed

sealed class ImportFromSeedEvent {
    data class SeedPhraseEntered(val seeds: String) : ImportFromSeedEvent()
    data class NewPasswordChange(val newPassword: String) : ImportFromSeedEvent()
    data class ConfirmPasswordChange(val confirmPassword: String) : ImportFromSeedEvent()
    data class SignInWithFaceIdChange(val isFaceIdChange: Boolean) : ImportFromSeedEvent()
    data class NewPasswordToggle(val passwordToggle: Boolean) : ImportFromSeedEvent()
    data class ConfirmPasswordToggle(val confirmPasswordToggle: Boolean) : ImportFromSeedEvent()
    data object ImportBtnClick : ImportFromSeedEvent()
}