package com.app.ezipaycoin.presentation.success

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.composables.GradientText
import com.app.ezipaycoin.ui.composables.TopAppBarWithProgressIndicator
import com.app.ezipaycoin.ui.theme.AppBackgroundColor

@Composable
fun WalletSuccess(
    navController: NavController,
    walletSharedViewModel: WalletSharedViewModel
) {

    Scaffold(
        containerColor = AppBackgroundColor,
        topBar = {
            TopAppBarWithProgressIndicator(currentStep = 3, totalSteps = 3) {
                navController.popBackStack()
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GoldGradientButton(
                    "Next",
                    onClick = {
                        navController.navigate(Screen.BottomNavScreens) {
                            popUpTo(Screen.Auth) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()), // Enable scrolling
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                // IMPORTANT: Replace with your actual shield image resource
                painter = painterResource(id = R.drawable.illus),
                contentDescription = "Secure Wallet Shield",
                modifier = Modifier
                    .fillMaxWidth(0.65f) // Adjust size as needed
                    .aspectRatio(1f / 1.15f), // Adjust based on your image's aspect ratio
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(40.dp))

            GradientText(
                "Success!",
                modifier = Modifier,
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight(400)),
                align = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You've successfully protected your wallet. Remember to keep your seed phrase safe, it's your responsibility!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ezipay cannot recover your wallet if you lose your Seed Phrase. You can find your seed phrase in\n" +
                        " Settings > Security & Privacy.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))


        }
    }
}

//@Preview
//@Composable
//fun CreatePasswordPreview() {
//    EzipayCoinTheme {
//        WalletSuccess(navController = rememberNavController())
//    }
//}