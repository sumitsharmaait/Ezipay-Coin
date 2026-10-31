package com.app.ezipaycoin.presentation.importfromseed

import androidx.compose.ui.text.input.ImeAction
import com.app.ezipaycoin.utils.SecureScreen
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.composables.AppTextField
import com.app.ezipaycoin.ui.composables.FaceIdToggleRow
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.GoldTextColor
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.utils.BiometricHelper
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.pasteFromClipboard

private val HorizontalScreenPadding = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportFromSeedScreen(
    navController: NavController,
    viewModel: ImportFromSeedViewModel
) {
    SecureScreen()
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManger = LocalFocusManager.current
    val canUseBiometric = BiometricHelper.canAuthenticate(context)

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
        topBar = { ImportFromSeedTopBar(onBackClick = { navController.navigateUp() }) },
        bottomBar = {
            ImportButton(
                onClick = {
                    focusManger.clearFocus()
                    keyboardController?.hide()
                    viewModel.onEvent(ImportFromSeedEvent.ImportBtnClick)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = HorizontalScreenPadding)
                .verticalScroll(rememberScrollState()), // Enable scrolling for smaller screens
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Seed Phrase Input
            SeedPhraseField(
                seedPhrase = state.seedPhrase,
                onSeedPhraseChange = { viewModel.onEvent(ImportFromSeedEvent.SeedPhraseEntered(it)) },
                onPasteClick = { context.pasteFromClipboard() }
            )
            FieldErrorText(error = state.seedPhraseError)

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
                    viewModel.onEvent(ImportFromSeedEvent.NewPasswordToggle(it))
                }
            )
            FieldErrorText(error = state.newPasswordError)

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
                    viewModel.onEvent(ImportFromSeedEvent.ConfirmPasswordToggle(it))
                }
            )
            FieldErrorText(error = state.confirmPasswordError)

            Spacer(modifier = Modifier.height(32.dp))

            // Sign in with Face ID
            if (canUseBiometric) {
                FaceIdToggleRow(
                    label = "Sign in with Face ID?",
                    checked = state.signInWithFaceId,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            BiometricHelper.authenticate(
                                context as FragmentActivity,
                                title = "Confirm Fingerprint",
                                subtitle = "Touch the sensor to enable biometric login",
                                onSuccess = {
                                    viewModel.onEvent(
                                        ImportFromSeedEvent.SignInWithFaceIdChange(
                                            true
                                        )
                                    )
                                },
                                onFailure = {
                                    viewModel.onEvent(
                                        ImportFromSeedEvent.SignInWithFaceIdChange(
                                            false
                                        )
                                    )
                                }
                            )
                        }
                    }
                )
            }

            if (state.bioMetricMessage.isNotBlank()) {
                Text(
                    text = state.bioMetricMessage,
                    style = MaterialTheme.typography.titleSmall.copy(color = OnboardingGold1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Terms and Conditions
            TermsAndConditionsText()

            Spacer(modifier = Modifier.height(24.dp)) // Space before bottom button

            if (state.responseState is ResponseState.Loading) {
                CircularProgressIndicator(color = OnboardingGold1)
            } else if (state.responseState is ResponseState.Error) {
                FieldErrorText(error = (state.responseState as ResponseState.Error).message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportFromSeedTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text("Import From Seed", style = MaterialTheme.typography.bodyMedium) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
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
}

@Composable
private fun ImportButton(onClick: () -> Unit) {
    GoldGradientButton(
        label = "Import",
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 8.dp)
    )
}

@Composable
private fun SeedPhraseField(
    seedPhrase: String,
    onSeedPhraseChange: (String) -> Unit,
    onPasteClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppTextField(
            value = seedPhrase,
            onValueChange = onSeedPhraseChange,
            label = "Seed Phrase",
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            minLines = 2,
            isTextArea = true,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onPasteClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_scanner),
                contentDescription = "Scan Seed Phrase",
                tint = TextHintColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun FieldErrorText(error: String) {
    if (error.isNotBlank()) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 4.dp)
        )
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
