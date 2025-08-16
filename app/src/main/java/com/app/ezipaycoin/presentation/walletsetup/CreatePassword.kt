package com.app.ezipaycoin.presentation.walletsetup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.composables.AppTextField
import com.app.ezipaycoin.ui.composables.FaceIdToggleRow
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.composables.GradientText
import com.app.ezipaycoin.ui.composables.TopAppBarWithProgressIndicator
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.GoldTextColor
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor

@Composable
fun CreatePassword(
    navController: NavController
) {
    var newPassword by rememberSaveable { mutableStateOf("") }
    var newPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var signInWithFaceId by rememberSaveable { mutableStateOf(false) } // Default to off
    var understandRecovery by rememberSaveable { mutableStateOf(false) }

    // Basic password strength logic (UI only)
    val passwordStrength = when {
        newPassword.isEmpty() -> "" // No text when empty
        newPassword.length < 8 -> "Weak"
        newPassword.any { it.isDigit() } && newPassword.any { it.isLetter() } -> "Good"
        else -> "Medium"
    }
    val passwordStrengthColor = when (passwordStrength) {
        "Weak" -> Color.Red
        "Medium" -> GoldTextColor
        "Good" -> GoldTextColor // Or a greenish gold if desired
        else -> TextHintColor
    }


    Scaffold(
        containerColor = AppBackgroundColor,
        topBar = {
            TopAppBarWithProgressIndicator(currentStep = 1, totalSteps = 3) {
                /* TODO: Handle on back click */
            }
        },
        bottomBar = {
            GoldGradientButton(
                "Secure My Wallet",
                onClick = { navController.navigate(Screen.Auth.CreateNewWallet) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()), // Enable scrolling
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            GradientText(
                text = "Create Password",
                modifier = Modifier,
                style = MaterialTheme.typography.titleLarge,
                align = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Set a secure and memorable password to unlock your Ezipay Wallet safely.",
                style = MaterialTheme.typography.titleMedium.copy(color = TextHintColor),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // New Password Input
            AppTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "New Password",
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(
                    mask = '*'
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPasswordToggleEnabled = true,
                passwordVisible = newPasswordVisible,
                onPasswordVisibilityChange = { newPasswordVisible = it }
            )
            if (passwordStrength.isNotEmpty()) {
                Text(
                    text = "Password strength: $passwordStrength",
                    style = MaterialTheme.typography.titleSmall.copy(color = passwordStrengthColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }


            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Password Input (label is "New Password" again in image)
            AppTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "New Password", // Matches image
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(
                    mask = '*'
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPasswordToggleEnabled = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordVisibilityChange = { confirmPasswordVisible = it }
            )
            Text(
                text = "Must be at least 8 characters",
                style = MaterialTheme.typography.titleSmall.copy(color = TextHintColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign in with Face ID
            FaceIdToggleRow(
                label = "Sign in with Face ID?",
                checked = signInWithFaceId,
                onCheckedChange = { signInWithFaceId = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // "I understand" Checkbox
            StyledCheckboxRow(
                checked = understandRecovery,
                onCheckedChange = { understandRecovery = it },
                text = buildAnnotatedString {
                    append("I understand EziPay Coin cannot recover this password. ")
                    pushStringAnnotation(tag = "LEARN_MORE", annotation = "learn_more_url")
                    withStyle(
                        style = SpanStyle(
                            color = GoldTextColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append("Learn more")
                    }
                    pop()
                },
                onTextClick = { offset ->
                    // Handle "Learn more" click
                    understandRecovery.toString() // Placeholder, to use the variable
                    // In a real app, check if offset is within the "Learn more" part
                    // For simplicity, can make the whole text clickable for "Learn more" for now
                    // or use more advanced text click detection.
                }
            )

            Spacer(modifier = Modifier.height(24.dp)) // Space before bottom button
        }
    }
}




@Composable
fun StyledCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: androidx.compose.ui.text.AnnotatedString,
    onTextClick: (Int) -> Unit // Offset for click detection if needed
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }, // Click row to toggle checkbox
        verticalAlignment = Alignment.Top // Align checkbox with top of text
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = GoldAccentColor,
                uncheckedColor = Color(0xFF757575), // Border color when unchecked
                checkmarkColor = Color.White // Color of the checkmark itself
            ),
            modifier = Modifier.size(24.dp) // Standard size
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextPrimaryColor,
                lineHeight = 18.sp
            ),
            modifier = Modifier.clickable { onTextClick(0) } // Simplistic click handler for whole text
        )
    }
}


@Preview
@Composable
fun CreatePasswordPreview() {
    EzipayCoinTheme {
        CreatePassword(navController = rememberNavController())
    }
}




