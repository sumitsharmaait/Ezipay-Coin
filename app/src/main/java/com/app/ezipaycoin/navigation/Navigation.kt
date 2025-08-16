package com.app.ezipaycoin.navigation


import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel

@Composable
fun Navigation(
    isLoggedIn: Boolean,
    navHostController: NavHostController,
    apiService: ApiService,
    walletSharedViewModel: WalletSharedViewModel
) {

    NavHost(
        navController = navHostController,
        startDestination = if (isLoggedIn) Screen.BottomNavScreens else Screen.Auth,
        enterTransition = {
            fadeIn(animationSpec = tween(1000))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(1000))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(1000))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(1000))
        }
    )
    {
        authNavGraph(navHostController)
        bottomNavGraph(navHostController, apiService, walletSharedViewModel)
        appNavGraph(navHostController, apiService, walletSharedViewModel)
    }


}