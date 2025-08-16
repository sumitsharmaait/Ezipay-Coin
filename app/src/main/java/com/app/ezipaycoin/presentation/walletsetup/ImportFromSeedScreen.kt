package com.app.ezipaycoin.presentation.walletsetup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.ui.composables.FaceIdToggleRow
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.GoldTextColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.grey_22

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportFromSeedScreen(navController: NavController) {
    var seedPhrase by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var newPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var signInWithFaceId by rememberSaveable { mutableStateOf(true) } // Default to on as in image

    Scaffold(
        containerColor = AppBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Import From Seed", style = MaterialTheme.typography.bodyMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBackgroundColor,
                    titleContentColor = TextPrimaryColor
                )
            )
        },
        bottomBar = {
            GoldGradientButton(
                label = "Import",
                onClick = { /* TODO: Handle import logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 8.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()), // Enable scrolling for smaller screens
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Seed Phrase Input
            Row(verticalAlignment = Alignment.CenterVertically) {

                AppTextField(
                    value = seedPhrase,
                    onValueChange = { seedPhrase = it },
                    label = "Seed Phrase",
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isPasswordToggleEnabled = true,
                    passwordVisible = newPasswordVisible,
                    onPasswordVisibilityChange = { newPasswordVisible = it },
                    minLines = 3,
                    modifier = Modifier.weight(1f),
                )

//                AppTextField(
//                    value = seedPhrase,
//                    onValueChange = { seedPhrase = it },
//                    label = "Seed Phrase",
//                    modifier = Modifier.weight(1f),
//                    trailingIcon = { Icons.Filled.Visibility },
//                    isTextArea = true,
//                    isPasswordToggleEnabled = true,
//                    passwordVisible = newPasswordVisible,
//                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                    minLines = 3 // As it looks like a multi-line input
//                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { /* TODO: Handle Scan Seed Phrase */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scanner),
                        contentDescription = "Scan Seed Phrase",
                        tint = TextHintColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(20.dp))

            // New Password Input
            AppTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "New Password",
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPasswordToggleEnabled = true,
                passwordVisible = newPasswordVisible,
                onPasswordVisibilityChange = { newPasswordVisible = it }
            )
            Text(
                text = "Must be at least 8 characters",
                style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Password Input
            AppTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPasswordToggleEnabled = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordVisibilityChange = { confirmPasswordVisible = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign in with Face ID
            FaceIdToggleRow(
                label = "Sign in with Face ID?",
                checked = signInWithFaceId,
                onCheckedChange = { signInWithFaceId = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Terms and Conditions
            TermsAndConditionsText()

            Spacer(modifier = Modifier.height(24.dp)) // Space before bottom button
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null, // Custom trailing icon if needed
    isPasswordToggleEnabled: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: ((Boolean) -> Unit)? = null,
    isTextArea: Boolean = false,
    minLines: Int = 1
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, grey_22, RoundedCornerShape(12.dp))
            .then(if (isTextArea) Modifier.defaultMinSize(minHeight = (minLines * 48).dp) else Modifier), // Approximate height
        label = { Text(label, color = TextHintColor) },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        singleLine = !isTextArea,
        minLines = if (isTextArea) minLines else 1,
        colors = TextFieldDefaults.colors(
            focusedTextColor = TextPrimaryColor,
            unfocusedTextColor = TextPrimaryColor,
            disabledTextColor = TextHintColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            cursorColor = GoldAccentColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLabelColor = TextHintColor, // Keep label color consistent
            unfocusedLabelColor = TextHintColor,
        ),
        trailingIcon = if (isPasswordToggleEnabled && onPasswordVisibilityChange != null) {
            {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"
                IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                    Icon(imageVector = image, description, tint = TextHintColor)
                }
            }
        } else trailingIcon
    )
}

//@Composable
//fun FaceIdToggleRow(
//    checked: Boolean,
//    onCheckedChange: (Boolean) -> Unit
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text(
//            text = "Sign in with Face ID?",
//            style = MaterialTheme.typography.bodyMedium
//        )
//        Switch(
//            checked = checked,
//            onCheckedChange = onCheckedChange,
//            colors = SwitchDefaults.colors(
//                checkedThumbColor = Color.White,
//                checkedTrackColor = GoldAccentColor,
//                uncheckedThumbColor = Color(0xFFE0E0E0),
//                uncheckedTrackColor = Color(0xFF757575), // A bit lighter than text field bg
//                checkedBorderColor = Color.Transparent,
//                uncheckedBorderColor = Color.Transparent
//            )
//        )
//    }
//}

@Composable
fun TermsAndConditionsText() {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color.White)) {
            append("By proceeding, you agree to these ")
        }
        pushStringAnnotation(tag = "TERMS", annotation = "terms_url") // Use URL or identifier
        withStyle(style = SpanStyle(color = GoldTextColor, textDecoration = TextDecoration.Underline)) {
            append("Term and Conditions.")
        }
        pop()
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Start),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                annotatedString
                    .getStringAnnotations(tag = "TERMS", start = 0, end = annotatedString.length)
                    .firstOrNull()
                    ?.let {
                        // TODO: Handle click, e.g., open terms URL
                        println("Clicked on: ${it.item}")
                    }
            }
    )
}

@Composable
fun ImportButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val goldGradient = Brush.linearGradient(
        colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
    )
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(50)) // Fully rounded
            .background(goldGradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Import",
            color = Color.Black,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview
@Composable
fun ImportFromSeedPreview() {
    EzipayCoinTheme {
        ImportFromSeedScreen(navController = rememberNavController())
    }
}





