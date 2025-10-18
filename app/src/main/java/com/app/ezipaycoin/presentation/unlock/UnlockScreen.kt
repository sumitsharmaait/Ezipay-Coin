package com.app.ezipaycoin.presentation.unlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.AppTextField
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.composables.GradientText
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.utils.BiometricHelper
import com.app.ezipaycoin.utils.ResponseState

@Composable
fun UnlockScreen(
    onUnlocked: () -> Unit,
    viewModel: UnlockScreenVM
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val canUseBiometric = BiometricHelper.canAuthenticate(context)

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UnlockScreenVMEvent.Unlocked -> {
                    onUnlocked()
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppBackgroundColor)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()), // Enable scrolling
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            GradientText(
                text = "Unlock App",
                modifier = Modifier,
                style = MaterialTheme.typography.titleLarge,
                align = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please use any option provided below to unlock app.",
                style = MaterialTheme.typography.titleMedium.copy(color = TextHintColor),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            AppTextField(
                value = state.password,
                onValueChange = { viewModel.onEvent(UnlockScreenEvent.OnPasswordChange(it)) },
                label = "Enter Password",
                visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(
                    mask = '*'
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPasswordToggleEnabled = true,
                passwordVisible = state.passwordVisible,
                onPasswordVisibilityChange = {
                    viewModel.onEvent(
                        UnlockScreenEvent.PasswordToggle
                    )
                }
            )
            if (state.passwordError.isNotBlank()) {
                Text(
                    text = state.passwordError,
                    style = MaterialTheme.typography.titleSmall.copy(color = Color.Red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            GoldGradientButton(
                "Unlock",
                onClick = { viewModel.onEvent(UnlockScreenEvent.OnSubmit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            if (canUseBiometric && state.bioAuthEnabled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp,
                        color = GoldAccentColor
                    )
                    Text(
                        "Or Use BioMetric",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp,
                        color = GoldAccentColor
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                AppGreyButton(
                    labelColor = Color.White,
                    label = "BioMetric",
                    onClick = {
                        BiometricHelper.authenticate(
                            activity = activity,
                            title = "Confirm Fingerprint",
                            subtitle = "Touch the sensor to unlock",
                            onSuccess = { viewModel.onEvent(UnlockScreenEvent.OnBioAuthSuccess) },
                            onFailure = { }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp)
                )
            }
        }

        if (state.responseState is ResponseState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)), // optional: dim background
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OnboardingGold1)
            }
        }

    }


}