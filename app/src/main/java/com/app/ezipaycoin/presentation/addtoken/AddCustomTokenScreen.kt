package com.app.ezipaycoin.presentation.addtoken

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ezipaycoin.ui.composables.Dialogue
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.utils.ResponseState

@Composable
fun AddCustomTokenScreen(
    navController: NavController,
    viewModel: AddTokenViewModel,
    onTokenAdded: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    when (state.addResponse) {
        is ResponseState.Success -> {
            Dialogue(isError = false, msg = "Token added successfully") {
                viewModel.onEvent(AddTokenEvent.DismissDialog(1))
                onTokenAdded()
            }
        }

        is ResponseState.Error -> {
            Dialogue(
                isError = true,
                msg = (state.addResponse as ResponseState.Error).message
            ) {
                viewModel.onEvent(AddTokenEvent.DismissDialog(1))
            }
        }

        else -> Unit
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Add custom token",
                style = MaterialTheme.typography.headlineSmall.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                    ),
                    fontWeight = FontWeight(800)
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Paste a contract address — we'll try to fill in the details automatically.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryColor)
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.contractAddress,
                onValueChange = { viewModel.onEvent(AddTokenEvent.ContractAddressChanged(it)) },
                label = { Text("Contract address (0x…)") },
                singleLine = true,
                isError = state.contractAddress.isNotBlank() && !state.isAddressValidFormat,
                modifier = Modifier.fillMaxWidth()
            )

            // auto-fetch feedback
            when (val v = state.validateResponse) {
                is ResponseState.Loading -> {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = OnboardingGold1,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            "Detecting token…",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimaryColor)
                        )
                    }
                }

                is ResponseState.Error -> {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        v.message,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE0A100))
                    )
                }

                is ResponseState.Success -> {
                    if (state.autoFilled) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = OnboardingGold1,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                "Auto-filled — edit below if needed",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnboardingGold1)
                            )
                        }
                    }
                }

                ResponseState.Idle -> Unit
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.symbol,
                onValueChange = { viewModel.onEvent(AddTokenEvent.SymbolChanged(it)) },
                label = { Text("Symbol") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.tokenName,
                onValueChange = { viewModel.onEvent(AddTokenEvent.TokenNameChanged(it)) },
                label = { Text("Token name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.decimals,
                onValueChange = { viewModel.onEvent(AddTokenEvent.DecimalsChanged(it)) },
                label = { Text("Decimals") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            val adding = state.addResponse is ResponseState.Loading
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (state.isFormValid && !adding)
                            Brush.linearGradient(listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4))
                        else
                            Brush.linearGradient(listOf(greyButtonBackground, greyButtonBackground))
                    )
                    .clickable(enabled = state.isFormValid && !adding) {
                        viewModel.onEvent(AddTokenEvent.AddToken)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (adding) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Add token",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (state.isFormValid) Color.Black else TextPrimaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}