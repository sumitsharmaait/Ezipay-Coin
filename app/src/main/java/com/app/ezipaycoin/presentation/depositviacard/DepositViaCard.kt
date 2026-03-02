package com.app.ezipaycoin.presentation.depositviacard

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ezipaycoin.ui.composables.AppTextField
import com.app.ezipaycoin.ui.composables.AppTextFieldValue
import com.app.ezipaycoin.ui.composables.Dialogue
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.utils.CardFormatter.formatCardNumber
import com.app.ezipaycoin.utils.ExpiryFormatter
import com.app.ezipaycoin.utils.ResponseState

@Composable
fun DepositViaCard(
    navController: NavController,
    viewModel: DepositViaCardViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    when (state.walletInfoResponse) {
        is ResponseState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OnboardingGold1)
            }
        }

        is ResponseState.Success -> {
            val data = (state.walletInfoResponse as ResponseState.Success<String>).data
            DepositeScreenSuccess(viewModel = viewModel, onSuccess = {
                open3DS(context, it)
            })
        }

        is ResponseState.Error -> {
            Dialogue(
                isError = true,
                msg = (state.walletInfoResponse as ResponseState.Error).message
            ) {
                viewModel.onEvent(DepositeViaCardEvent.DismissDialog(0))
            }
        }

        ResponseState.Idle -> {
            Log.e("Idle", "Idle")
        }
    }


}

@Composable
private fun DepositeScreenSuccess(
    viewModel: DepositViaCardViewModel,
    onSuccess: (url: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context: Context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = state.networkInfo,
            style = MaterialTheme.typography.headlineSmall.copy(
                brush = Brush.linearGradient(
                    colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                ), fontWeight = FontWeight(800)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        AppTextFieldValue(
            value = state.cardNo,
            onValueChange = { newValue ->
                viewModel.onEvent(
                    DepositeViaCardEvent.OnCardNoChanged(
                        formatCardNumber(state.cardNo, newValue)
                    )
                )
            },
            label = "Enter Card Number",
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        if (state.cardNoError.isNotBlank()) {
            Text(
                text = state.cardNoError,
                style = MaterialTheme.typography.titleSmall.copy(color = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Column(modifier = Modifier.weight(1f)) {
                AppTextFieldValue(
                    value = state.expiryMonthYear,
                    onValueChange = { newValue ->
                        viewModel.onEvent(
                            DepositeViaCardEvent.OnExpiryMonthYearChanged(
                                ExpiryFormatter.formatExpiry(
                                    oldValue = state.expiryMonthYear,
                                    newValue = newValue
                                )
                            )
                        )
                    },
                    label = "MM/YY",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                        autoCorrectEnabled = false,
                    )
                )
                if (state.expiryMonthYearError.isNotBlank()) {
                    Text(
                        text = state.expiryMonthYearError,
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.Red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                AppTextField(
                    value = state.cvv,
                    onValueChange = {
                        if (it.length <= 4 && it.all(Char::isDigit)) {
                            viewModel.onEvent(
                                DepositeViaCardEvent.OnCvvChanged(it)
                            )
                        }
                    },
                    label = "CVV",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    isPasswordToggleEnabled = false,
                )
                if (state.cvvError.isNotBlank()) {
                    Text(
                        text = state.cvvError,
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.Red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

        }


        Spacer(modifier = Modifier.height(8.dp))


        AppTextField(
            value = state.accountName,
            onValueChange = {
                viewModel.onEvent(DepositeViaCardEvent.OnAccountNameChanged(it))
            },
            label = "Name (As it appears on your card)",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            isPasswordToggleEnabled = false,
        )
        if (state.accountNameError.isNotBlank()) {
            Text(
                text = state.accountNameError,
                style = MaterialTheme.typography.titleSmall.copy(color = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AppTextField(
            value = state.amount,
            onValueChange = {
                viewModel.onEvent(DepositeViaCardEvent.OnAmountChanged(it))
            },
            label = "Amount (Min 1 USD and Max 700 USD)",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            isPasswordToggleEnabled = false,
        )
        if (state.amountError.isNotBlank()) {
            Text(
                text = state.amountError,
                style = MaterialTheme.typography.titleSmall.copy(color = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AppTextField(
            value = state.remarks,
            onValueChange = {
                viewModel.onEvent(DepositeViaCardEvent.OnRemarksChanged(it))
            },
            label = "Remarks",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            isPasswordToggleEnabled = false,
        )
        if (state.remarksError.isNotBlank()) {
            Text(
                text = state.remarksError,
                style = MaterialTheme.typography.titleSmall.copy(color = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (state.depositeResponse) {
            is ResponseState.Loading -> {
                CircularProgressIndicator(color = OnboardingGold1)
            }

            is ResponseState.Success -> {
                val res = state.depositeResponse as ResponseState.Success
                onSuccess(res.data)
                viewModel.onEvent(DepositeViaCardEvent.DismissDialog(1))
            }

            is ResponseState.Error -> {
                Dialogue(
                    isError = true,
                    msg = (state.depositeResponse as ResponseState.Error).message
                ) {
                    viewModel.onEvent(DepositeViaCardEvent.DismissDialog(1))
                }
            }

            ResponseState.Idle -> {}
        }

        GoldGradientButton(
            label = "Deposit",
            onClick = { viewModel.onEvent(DepositeViaCardEvent.OnDepositSubmit) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 8.dp)
        )

    }
}

fun open3DS(context: Context, url: String) {
    val cleanUrl = url
        .replace("\\/", "/")
        .replace("\\", "")
        .trim()

    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()

    customTabsIntent.launchUrl(
        context,
        Uri.parse(cleanUrl)
    )
}


