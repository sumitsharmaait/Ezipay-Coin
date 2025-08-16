package com.app.ezipaycoin.presentation.walletsetup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.R.drawable.ezipay_wallet
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.composables.GradientText
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.GoldTextColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4


@Composable
fun WalletSetUp(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) { // Main Box for background and orb positioning
        // Decorative Orbs (positioned absolutely)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp)) // Adjust for status bar and top spacing

            // Wallet Image
            Image(
                painter = painterResource(id = ezipay_wallet), // Replace with your actual drawable
                contentDescription = "Ezipay Wallet Graphic",
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Adjust size as needed
                    .aspectRatio(1.0f), // Adjust based on your image's aspect ratio
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(40.dp))

            // "Ezipay Wallet" Text

            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Ezipay",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight(400)),
                    color = Color.White,
                )
                GradientText(
                    text = "Wallet",
                    modifier = Modifier,
                    style = MaterialTheme.typography.displayMedium,
                    align = TextAlign.Center
                )
            }

//            Text(
//                buildAnnotatedString {
//                    withStyle(
//                        style = SpanStyle(
//                            color = TextPrimaryColor,
//                            fontWeight = FontWeight(400),
//                            fontSize = MaterialTheme.typography.displayMedium.fontSize
//                        )
//                    ) {
//                        append("Ezipay ")
//                    }
//                    withStyle(
//                        style = SpanStyle(
//                            color = GoldTextColor,
//                            fontWeight = FontWeight(400),
//                            fontSize = MaterialTheme.typography.displayMedium.fontSize
//                        )
//                    ) {
//                        append("Wallet")
//                    }
//                },
//                style = MaterialTheme.typography.headlineLarge
//            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Text
            Text(
                text = "  The best crypto wallet app\nof this century  ",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                AppGreyButton(
                    labelColor = Color.White,
                    label = "Import Seed",
                    onClick = { navController.navigate(Screen.Auth.ImportFromSeed) },
                    modifier = Modifier
                        .weight(1f)
                )

//                Button(
//                    onClick = { navController.navigate(Screen.Auth.ImportFromSeed) },
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(50.dp)
//                        .clip(RoundedCornerShape(80)),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = greyButtonBackground,
//                        contentColor = TextPrimaryColor
//                    )
//                ) {
//                    Text(
//                        "Import Seed",
//                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight(500))
//                    )
//                }

                GoldGradientButton(
                    label = "Create New",
                    onClick = { navController.navigate(Screen.Auth.CreatePassword) },
                    modifier = Modifier
                        .weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // "Or Login with" Divider
            OrLoginWithDivider()

            Spacer(modifier = Modifier.height(30.dp))

            // Social Login Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly // Or Arrangement.spacedBy(16.dp)
            ) {
                SocialLoginButton(
                    iconRes = R.drawable.sign_in_facebook,
                    contentDescription = "Login with Facebook"
                )
                SocialLoginButton(
                    iconRes = R.drawable.sign_in_google,
                    contentDescription = "Login with Google"
                )
                SocialLoginButton(
                    iconRes = R.drawable.sign_in_apple,
                    contentDescription = "Login with Apple"
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Push bottom content down

            // "Don't have an account?" Text
            ClickableRegisterNowText()

            Spacer(modifier = Modifier.height(40.dp)) // Bottom padding
        }
    }
}

@Composable
fun CreateNewWalletButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val goldGradient = Brush.verticalGradient(
        colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
    )
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(goldGradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Create New\nWallet",
            color = Color.Black, // Assuming black text from image
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )
        )
    }
}

@Composable
fun OrLoginWithDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = GoldAccentColor)
        Text(
            "Or Login with",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = GoldAccentColor)
    }
}

@Composable
fun SocialLoginButton(iconRes: Int, contentDescription: String, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(width = 80.dp, height = 45.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Unspecified // Let icon tint itself
        ),// Adjust size as needed
        contentPadding = PaddingValues(0.dp) // Remove default padding to center icon properly
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize()// Adjust icon size
        )
    }
}

@Composable
fun ClickableRegisterNowText() {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight(500))) {
                append("Don't have an account? ")
            }
            withStyle(style = SpanStyle(color = GoldTextColor, fontWeight = FontWeight(700))) {
                append("Register Now")
            }
        },
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.clickable { /* Handle Register Now click */ }
    )
}


@Preview
@Composable
fun PayScreenUtilitiesPreview() {
    EzipayCoinTheme {
       WalletSetUp(navController = rememberNavController())
    }
}
