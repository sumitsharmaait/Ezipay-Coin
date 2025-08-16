package com.app.ezipaycoin.presentation.walletsetup

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.composables.GradientText
import com.app.ezipaycoin.ui.composables.TopAppBarWithProgressIndicator
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.ui.theme.GoldTextColor
import com.app.ezipaycoin.ui.theme.TextHintColor

@Composable
fun CreateNewWallet(
    navController: NavController
) {

    Scaffold(
        containerColor = AppBackgroundColor,
        topBar = {
            TopAppBarWithProgressIndicator(currentStep = 2, totalSteps = 3) {
                /* TODO: Handle on back click */
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GradientText(
                    text = "Remind Me Later",
                    modifier = Modifier
                        .clickable { /* TODO: Handle Remind Me Later */ }
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                    align = TextAlign.Center
                )
//                Text(
//                    text = "Remind Me Later",
//                    style = MaterialTheme.typography.bodyMedium.copy(
//                        color = GoldTextColor,
//                        fontWeight = FontWeight(700)
//                    ),
//                    modifier = Modifier
//                        .clickable { /* TODO: Handle Remind Me Later */ }
//                        .padding(vertical = 8.dp)
//                )
                Spacer(modifier = Modifier.height(12.dp))
                GoldGradientButton(
                    "Secure Now",
                    onClick = { navController.navigate(Screen.Auth.SecureWallet) },
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

            Text(
                text = "Secure Your Wallet",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val descriptionText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextHintColor)) {
                    append("Don't risk losing your funds. Protect your wallet by saving your ")
                }
                withStyle(style = SpanStyle(color = GoldTextColor, fontWeight = FontWeight(600))) {
                    append("Seed phrase")
                }
                withStyle(style = SpanStyle(color = TextHintColor)) {
                    append(" in a place you trust.\n\nIt's the only way to recover your wallet if you get locked out of the app or get a new device.")
                }
            }
            Text(
                text = descriptionText,
                style = MaterialTheme.typography.titleMedium
            )

        }
    }
}

@Preview
@Composable
fun CreateNewWalletPreview() {
    EzipayCoinTheme {
        CreateNewWallet(navController = rememberNavController())
    }
}




