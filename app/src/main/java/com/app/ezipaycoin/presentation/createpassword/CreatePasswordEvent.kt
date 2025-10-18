package com.app.ezipaycoin.presentation.createpassword

sealed class CreatePasswordEvent {
    data class NewPasswordChange(val newPassword: String) : CreatePasswordEvent()
    data class ConfirmPasswordChange(val confirmPassword: String) : CreatePasswordEvent()
    data class SignInWithFaceIdChange(val isFaceIdChange: Boolean) : CreatePasswordEvent()
    data class IUnderstandChange(val iUnderstandChange: Boolean) : CreatePasswordEvent()
    data class NewPasswordToggle(val passwordToggle: Boolean) : CreatePasswordEvent()
    data class ConfirmPasswordToggle(val confirmPasswordToggle: Boolean) : CreatePasswordEvent()
    data object SecureBtnClick : CreatePasswordEvent()
}