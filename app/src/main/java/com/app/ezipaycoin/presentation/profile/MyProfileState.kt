package com.app.ezipaycoin.presentation.profile

data class MyProfileState(
    val profile: String = "",
    val walletAddress: String = "",
    val name: String = "Not Available",
    val email: String = "Not Available"
)
