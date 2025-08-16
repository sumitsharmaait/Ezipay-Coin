package com.app.ezipaycoin.presentation.dashboard.home

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.app.ezipaycoin.R
import com.app.ezipaycoin.data.remote.dto.QuickAction
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.presentation.shared.SharedEvent
import com.app.ezipaycoin.presentation.shared.SharedState
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.Dialogue
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.graphGreenColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.sendAmountTextColor
import com.app.ezipaycoin.utils.ResponseState

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    sharedViewModel: WalletSharedViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val sharedState by sharedViewModel.uiState.collectAsState()

//    LaunchedEffect(Unit) {
//        sharedViewModel.onEvent(SharedEvent.FetchBalance)
//    }

    when (sharedState.responseState) {
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
            val data =
                (sharedState.responseState as ResponseState.Success<BaseResponse<DashboardResponse>>).data.apiData
            HomeSuccessData(state, data, viewModel, sharedViewModel, sharedState, navController)
        }

        is ResponseState.Error -> {

        }

        ResponseState.Idle -> {
            Log.e("Idel", "Idle")
        }
    }

    if (state.comingSoonDialogue) {
        Dialogue(
            isError = false,
            msg = "Coming Soon"
        ) {
            viewModel.onEvent(HomeEvent.ComingSoonDialog(false))
        }
    }


}

@Composable
private fun HomeSuccessData(
    state: HomeState,
    data: DashboardResponse,
    viewModel: HomeViewModel,
    sharedViewModel: WalletSharedViewModel,
    sharedState: SharedState,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedCard(
            border = BorderStroke(width = 1.dp, color = greyButtonBackground),
            colors = CardDefaults.cardColors(containerColor = AppBackgroundColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(
                    top = 24.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 24.dp
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Est. Total Value",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* TODO: Change currency */ }
                    ) {
                        Text(
                            text = sharedState.selectedCrypto?.symbol ?: "",
                            modifier = Modifier.clickable {
                                viewModel.onEvent(
                                    HomeEvent.ExpandCryptoOptions(
                                        true
                                    )
                                )
                            },
                            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                        )

                        DropdownMenu(
                            expanded = state.isCurrencyOptionsExpanded,
                            onDismissRequest = {
                                viewModel.onEvent(
                                    HomeEvent.ExpandCryptoOptions(
                                        false
                                    )
                                )
                            }
                        ) {
                            data.crypto.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.symbol) },
                                    onClick = {
                                        viewModel.onEvent(HomeEvent.CryptoOptionChanged(item))
                                        sharedViewModel.onEvent(SharedEvent.CryptoOptionChanged(item))
                                    }
                                )
                            }
                        }

                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Select Currency",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (sharedState.selectedCrypto?.formattedBalance ?: ""),
                    style = MaterialTheme.typography.displayMedium.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                        )
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ("= $" + sharedState.selectedCrypto?.formattedFiatValue),
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GoldGradientButton(
                        label = "Send",
                        onClick = {
                            navController.navigate(Screen.AppNavScreens.Pay) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AppGreyButton(
                        labelColor = Color.White,
                        label = "Receive",
                        onClick = {
                            navController.navigate(Screen.AppNavScreens.Receive) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        val actions = listOf(
            QuickAction("Add Funds", Icons.Filled.AddCircleOutline),
            QuickAction("Pay Bills", Icons.AutoMirrored.Filled.ReceiptLong),
            QuickAction("Staking", Icons.Filled.StackedLineChart), // Or a custom icon
            QuickAction("Daily Rewards", Icons.Filled.Redeem),
            QuickAction("Gift Cards", Icons.Filled.CardGiftcard),
            QuickAction("More", Icons.Filled.Apps) // Or MoreHoriz
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            actions.forEach { action ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f) // Distribute space evenly
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedIconButton(
                        onClick = { viewModel.onEvent(HomeEvent.ComingSoonDialog(true)) },
                        modifier = Modifier,
                        shape = RoundedCornerShape(5.dp),
                        border = BorderStroke(1.dp, color = greyButtonBackground)
                    ) {
                        Icon(
                            action.icon,
                            contentDescription = action.label,
                            tint = Color.White,
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        action.label,
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.White),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedCard(
            border = BorderStroke(width = 1.dp, color = greyButtonBackground),
            colors = CardDefaults.cardColors(containerColor = AppBackgroundColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Asset",
                        modifier = Modifier
                            .weight(0.3f)
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor),
                        textAlign = TextAlign.Center
                    )

                    VerticalDivider(color = greyButtonBackground)

                    Text(
                        text = "Graph",
                        modifier = Modifier
                            .weight(0.4f)
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor),
                        textAlign = TextAlign.Center
                    )
                    VerticalDivider(color = greyButtonBackground)
                    Text(
                        text = "Change",
                        modifier = Modifier
                            .weight(0.3f)
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor),
                        textAlign = TextAlign.Center
                    )


                }

                HorizontalDivider(color = greyButtonBackground)

                // Asset Rows
                data.marketSnapshot.forEach { asset ->
                    AssetRow(asset = asset)
                    HorizontalDivider(color = greyButtonBackground)
                }
            }
        }

    }

}

@Composable
private fun AssetRow(asset: DashboardResponse.MarketSnapshot) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { /* TODO: Handle row click */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Asset Name Cell
        Text(
            text = asset.symbol,
            modifier = Modifier
                .weight(0.3f)
                .padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor),
            textAlign = TextAlign.Center
        )
        VerticalDivider(color = greyButtonBackground)

        // Graph Cell
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            SparklineGraph(
                data = asset.sparkline,
                graphColor = if (asset.changePct > 0) graphGreenColor else sendAmountTextColor,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            )
        }
        VerticalDivider(color = greyButtonBackground)

        // Price Cell
        Box(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = asset.changePct.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (asset.changePct > 0) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = if (asset.changePct > 0) "Price Up" else "Price Down",
                    tint = if (asset.changePct > 0) graphGreenColor else sendAmountTextColor,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(if (asset.changePct > 0) 45f else -45f)
                )
            }
        }
    }
}

@Composable
private fun SparklineGraph(
    data: List<Float>,
    graphColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 4f
) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val min = data.minOrNull() ?: 0f
        val max = data.maxOrNull() ?: 0f
        val range = if ((max - min) == 0f) 1f else max - min

        val path = Path()

        val startX = 0f
        val startY = size.height - ((data[0] - min) / range * size.height)
        path.moveTo(startX, startY)

        for (i in 1 until data.size) {
            val x = i * (size.width / (data.size - 1))
            val y = size.height - ((data[i] - min) / range * size.height)
            path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = graphColor,
            style = Stroke(width = strokeWidth)
        )
    }
}


//@Preview
//@Composable
//fun PreviewHome() {
//    EzipayCoinTheme {
//        HomeScreen(navController = rememberNavController())
//    }
//}