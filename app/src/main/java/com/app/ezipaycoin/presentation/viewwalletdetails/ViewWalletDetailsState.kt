package com.app.ezipaycoin.presentation.viewwalletdetails

data class ViewWalletDetailsState(
    val unlocked: Boolean = false,
    val privateKey: String = "",
    val seedPhrase: String = "",
    val password: String = "",
    val passwordError: String = "",
    val privateKeyRevealed: Boolean = false
)
