package com.app.ezipaycoin.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String) {

    @Serializable
    data object EzipaySplashScreen : Screen("Splash")

    @Serializable
    data object Auth {

        @Serializable
        data object WalkThrough

        @Serializable
        data object WalletSetup

        @Serializable
        data object ImportFromSeed

        @Serializable
        data object CreatePassword

        @Serializable
        data object CreateNewWallet

        @Serializable
        data object SecureWallet

        @Serializable
        data object WalletSuccess

        @Serializable
        data object SeedPhrase

        @Serializable
        data class SeedPhraseVerify(
            val phrases: List<String>
        )
    }

    @Serializable
    data object AppNavScreens {

        @Serializable
        data object Pay

        @Serializable
        data object MyProfile

        @Serializable
        data object Receive

        @Serializable
        data object Transactions

        @Serializable
        data object TransactionDetails

    }

    @Serializable
    data object BottomNavScreens {

        @Serializable
        data object Home

        @Serializable
        data object Wallet

        @Serializable
        data object Learn

        @Serializable
        data object Earn

    }


}