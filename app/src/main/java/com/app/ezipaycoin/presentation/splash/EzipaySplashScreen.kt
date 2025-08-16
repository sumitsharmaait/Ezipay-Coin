package com.app.ezipaycoin.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import kotlinx.coroutines.delay

@Composable
fun EzipaySplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = LocalContext.current) {
        delay(1500) // Splash delay (optional)
        if (state.isLoggedIn) {
            navController.navigate(Screen.BottomNavScreens.Home) {
                popUpTo(Screen.EzipaySplashScreen.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Auth.WalkThrough) {
                popUpTo(Screen.EzipaySplashScreen.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor) // Explicit black background for the Box
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_background_image),
            contentDescription = "Ezipay Splash Screen Graphic", // Descriptive text for accessibility
            modifier = Modifier.fillMaxSize(), // Make the image fill the Box
            contentScale = ContentScale.Fit
        )
    }
}


@Preview
@Composable
fun SplashScreenPreview() {
    EzipayCoinTheme {
        EzipaySplashScreen(rememberNavController(), viewModel<SplashViewModel>())
    }
}
