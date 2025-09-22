package com.app.ezipaycoin.presentation.importfromseed

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.composables.AppTextField
import com.app.ezipaycoin.ui.composables.FaceIdToggleRow
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.GoldTextColor
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.utils.pasteFromClipboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportFromSeedScreen(
    navController: NavController,
    viewModel: ImportFromSeedViewModel
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManger = LocalFocusManager.current


    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ImportFromSeedVMEvent.MoveToSuccess -> {
                    navController.navigate(Screen.Auth.WalletSuccess)
                }
            }
        }
    }

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
                onClick = {
                    focusManger.clearFocus()
                    keyboardController?.hide()
                    viewModel.onEvent(ImportFromSeedEvent.ImportBtnClick)
                },
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
                    value = state.seedPhrase,
                    onValueChange = { viewModel.onEvent(ImportFromSeedEvent.SeedPhraseEntered(it)) },
                    label = "Seed Phrase",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    minLines = 2,
                    isTextArea = true,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { context.pasteFromClipboard() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scanner),
                        contentDescription = "Scan Seed Phrase",
                        tint = TextHintColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            if (state.seedPhraseError.isNotBlank()) {
                Text(
                    text = state.seedPhraseError,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }


            Spacer(modifier = Modifier.height(20.dp))

            // New Password Input
            AppTextField(
                value = state.newPassword,
                onValueChange = { viewModel.onEvent(ImportFromSeedEvent.NewPasswordChange(it)) },
                label = "New Password",
                visualTransformation = if (state.newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPasswordToggleEnabled = true,
                passwordVisible = state.newPasswordVisible,
                onPasswordVisibilityChange = {
                    viewModel.onEvent(
                        ImportFromSeedEvent.NewPasswordToggle(
                            it
                        )
                    )
                }
            )
            if (state.newPasswordError.isNotBlank()) {
                Text(
                    text = state.newPasswordError,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }


            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Password Input
            AppTextField(
                value = state.confirmPassword,
                onValueChange = { viewModel.onEvent(ImportFromSeedEvent.ConfirmPasswordChange(it)) },
                label = "Confirm Password",
                visualTransformation = if (state.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPasswordToggleEnabled = true,
                passwordVisible = state.confirmPasswordVisible,
                onPasswordVisibilityChange = {
                    viewModel.onEvent(
                        ImportFromSeedEvent.ConfirmPasswordToggle(
                            it
                        )
                    )
                }
            )
            if (state.confirmPasswordError.isNotBlank()) {
                Text(
                    text = state.confirmPasswordError,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign in with Face ID
            FaceIdToggleRow(
                label = "Sign in with Face ID?",
                checked = state.signInWithFaceId,
                onCheckedChange = { viewModel.onEvent(ImportFromSeedEvent.SignInWithFaceIdChange(it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Terms and Conditions
            TermsAndConditionsText()

            Spacer(modifier = Modifier.height(24.dp)) // Space before bottom button
        }
    }
}

@Composable
fun TermsAndConditionsText() {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color.White)) {
            append("By proceeding, you agree to these ")
        }
        pushStringAnnotation(tag = "TERMS", annotation = "terms_url") // Use URL or identifier
        withStyle(
            style = SpanStyle(
                color = GoldTextColor,
                textDecoration = TextDecoration.Underline
            )
        ) {
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






