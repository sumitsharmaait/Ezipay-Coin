package com.app.ezipaycoin.presentation.dashboard.wallet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.presentation.shared.SharedEvent
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.AppTextField
import com.app.ezipaycoin.ui.composables.ComingSoonAlert
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.receivedAmountTextColor
import com.app.ezipaycoin.ui.theme.sendAmountTextColor

@Composable
fun WalletScreen(
    navController: NavController,
    viewModel: WalletViewModel,
    sharedViewModel: WalletSharedViewModel
) {

    val state by viewModel.uiState.collectAsState()
    val sharedState by sharedViewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    ComingSoonAlert(showDialog = showDialog) {
        showDialog = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppBackgroundColor)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedCard(
            border = BorderStroke(width = 1.dp, color = greyButtonBackground),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth(),
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 24.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Column {
                    Text(
                        "Total Balance",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        sharedState.selectedCrypto?.formattedFiatValue ?: "0.00",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                    )
                    // Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(
                        id = when (sharedState.selectedCrypto?.symbol) {
                            "EZPT" -> R.drawable.ic_ezipay_coin_small
                            "BNB" -> R.drawable.ic_currency_top_bar
                            "USDT" -> R.drawable.ic_usdt_icon
                            else -> R.drawable.ic_ezipay_coin_small
                        }
                    ),
                    contentDescription = "EPAY Logo",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    sharedState.selectedCrypto?.formattedBalance ?: "0.00",
                    style = MaterialTheme.typography.displayMedium.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                        )
                    )
                )
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = "Select Token",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { viewModel.onEvent(WalletEvent.ExpandCurrencyOptions(true)) }
                )

                sharedState.dashboardResponse?.crypto?.let {
                    DropdownMenu(
                        expanded = state.isCurrencyOptionsExpanded,
                        onDismissRequest = {
                            viewModel.onEvent(
                                WalletEvent.ExpandCurrencyOptions(
                                    false
                                )
                            )
                        },
                        offset = DpOffset(x = (-40).dp, y = 0.dp)
                    ) {
                        it.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.symbol) },
                                onClick = {
                                    sharedViewModel.onEvent(SharedEvent.CryptoOptionChanged(item))
                                    viewModel.onEvent(WalletEvent.CurrentOptionChanged(item))
                                }
                            )
                        }
                    }
                }


            }


        }

        Spacer(modifier = Modifier.height(16.dp))

        var searchQuery by remember { mutableStateOf("") }
        AppTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "Search tokens or address",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isPasswordToggleEnabled = false,
            trailingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "List of tokens",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
            )
            Text(
                "View All",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White),
                modifier = Modifier.clickable { /* TODO: Navigate to full token list */ }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        sharedState.dashboardResponse?.crypto?.let { crypt ->
            sharedState.dashboardResponse?.marketSnapshot?.let { snaps ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(greyButtonBackground, MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                ) {
                    crypt.forEachIndexed { index, token ->
                        TokenRow(tokenInfo = token, index, snaps)
                        if (index < crypt.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 0.dp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        AppGreyButton(
            labelColor = Color.White,
            label = "+ Add Token",
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
        )

    }

}


@Composable
private fun TokenRow(
    tokenInfo: DashboardResponse.Crypto,
    index: Int,
    marketSnapshot: List<DashboardResponse.MarketSnapshot>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to token details */ }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painterResource(
                id = when (tokenInfo.symbol) {
                    "EZPT" -> R.drawable.ic_ezipay_coin_small
                    "BNB" -> R.drawable.ic_currency_top_bar
                    "USDT" -> R.drawable.ic_usdt_icon
                    else -> R.drawable.ic_ezipay_coin_small
                }
            ), "Token", Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // Token Name
        Text(
            tokenInfo.symbol,
            style = MaterialTheme.typography.titleSmall.copy(color = Color.White),
            modifier = Modifier.weight(1f)
        )

        //Spacer(modifier = Modifier.width(8.dp))

        // Price and Percentage
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                tokenInfo.formattedBalance,
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
            )
            Text(
                text = if (marketSnapshot[index].changePct > 0) "+ " + marketSnapshot[index].changePct + "%" else
                    marketSnapshot[index].changePct.toString() + "%",
                style = MaterialTheme.typography.labelSmall.copy(color = if (marketSnapshot[index].changePct > 0) receivedAmountTextColor else sendAmountTextColor)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // USD Value
        Text(
            "~ $" + tokenInfo.formattedFiatValue,
            style = MaterialTheme.typography.titleSmall.copy(color = Color.White),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

