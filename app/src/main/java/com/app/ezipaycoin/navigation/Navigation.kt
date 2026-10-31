package com.app.ezipaycoin.navigation


import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.app.ezipaycoin.config.FeatureFlags
import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.repository.AppConfigRepoImpl
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel

@Composable
fun Navigation(
    isLoggedIn: Boolean,
    passwordCreated: Boolean,
    navHostController: NavHostController,
    apiService: ApiService,
    walletSharedViewModel: WalletSharedViewModel
) {

    // The route needs a signed-in user; an older server answers 404 and the build defaults stay.
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            // The session token may not be attached on the first try.
            repeat(3) { attempt ->
                if (FeatureFlags.refresh(AppConfigRepoImpl(apiService))) return@LaunchedEffect
                if (attempt < 2) delay(5_000)
            }
        }
    }

    NavHost(
        navController = navHostController,
        // A wallet without a password (an older sign-up path could leave one) goes through
        // password creation before the app opens.
        startDestination = when {
            isLoggedIn && !passwordCreated -> Screen.Auth.CreatePassword
            isLoggedIn -> Screen.BottomNavScreens
            else -> Screen.Auth
        },
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
        authNavGraph(navHostController, walletSharedViewModel)
        bottomNavGraph(navHostController, apiService, walletSharedViewModel)
        appNavGraph(navHostController, apiService, walletSharedViewModel)
    }


}