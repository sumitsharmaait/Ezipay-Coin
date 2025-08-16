package com.app.ezipaycoin.presentation

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School

import androidx.datastore.dataStore
import com.app.ezipaycoin.data.remote.dto.UserPreferencesSerializer
import com.app.ezipaycoin.ui.composables.BottomNavItem
import wallet.core.jni.HDWallet

class App : Application() {


    lateinit var clipboardManager: ClipboardManager
        private set

    companion object {
        private lateinit var instance: App
        fun getInstance(): App = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        System.loadLibrary("TrustWalletCore")
    }

    val dataStore by dataStore(
        fileName = "user-preferences",
        serializer = UserPreferencesSerializer
    )

    val wallet by lazy {
        HDWallet(128, "")
    }


    val items = listOf(
        BottomNavItem(
            "Home",
            Icons.Outlined.Home,
            Icons.Filled.Home
        ),
        BottomNavItem(
            "Wallet",
            Icons.Outlined.AccountBalanceWallet,
            Icons.Filled.AccountBalanceWallet
        ),
        BottomNavItem(
            "Pay",
            Icons.Outlined.AccountBalanceWallet,
            Icons.Filled.AccountBalanceWallet
        ),
        BottomNavItem(
            "Earn",
            Icons.AutoMirrored.Outlined.TrendingUp,
            Icons.AutoMirrored.Filled.TrendingUp
        ),
        BottomNavItem(
            "Learn",
            Icons.Outlined.School,
            Icons.Filled.School
        )
    )

    val navigationItems = listOf(
        BottomNavItem(
            "Profile",
            Icons.Filled.Person,
            Icons.Filled.Person),
        BottomNavItem(
            "Referrals & Rewards",
            Icons.Filled.WorkspacePremium,
            Icons.Filled.WorkspacePremium
        )
    )

}