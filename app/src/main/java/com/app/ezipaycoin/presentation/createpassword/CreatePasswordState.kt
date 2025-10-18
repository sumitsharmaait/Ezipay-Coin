package com.app.ezipaycoin.presentation.createpassword

data class CreatePasswordState(
    var newPassword: String = "",
    var newPasswordVisible: Boolean = false,
    var confirmPassword: String = "",
    var confirmPasswordVisible: Boolean = false,
    var signInWithFaceId: Boolean = false,
    var understandState: Boolean = false,
    var understandError: String = "",
    var newPasswordError: String = "",
    var confirmPasswordError: String = "",
    var passwordStrength: String = "",
    var bioMetricMessage: String = ""
)
