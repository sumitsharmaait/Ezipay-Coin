package com.app.ezipaycoin.presentation.main

data class MainState(
    val isLoggedIn: Boolean = false,
    val selectedBottomBar: String = "Home",
    val dataLoaded: Boolean = false,
    val bottomBarRoutes: List<String> = listOf(
        "Home",
        "Wallet",
        "Earn",
        "Learn"
    ),
    val topBarRoutes: List<String> = listOf(
        "Home",
        "Wallet",
        "Earn",
        "Learn",
        "Pay",
        "MyProfile",
        "Referrals & Rewards",
        "Receive"
    )
)
