package com.app.ezipaycoin.presentation.dashboard.home

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.app.ezipaycoin.ui.composables.ImageFromUrl
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

private object HomeDimens {
    val ScreenPadding = 16.dp
    val SectionSpacing = 18.dp
    val CardContentPadding = 16.dp
    val CardTopBottomPadding = 24.dp
    val ButtonSpacing = 12.dp
}

private object QuickActionLabels {
    const val ADD_FUNDS = "Add Funds"
    const val PAY_BILLS = "Pay Bills"
    const val STAKING = "Staking"
    const val DAILY_REWARDS = "Daily Rewards"
    const val GIFT_CARDS = "Gift Cards"
    const val MORE = "More"
}

private const val EXTERNAL_LINK = "https://www.ezipaycoin.com/"

private fun NavController.navigateSingleTop(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    sharedViewModel: WalletSharedViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val sharedState by sharedViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Refresh when the app returns to the foreground; the view model throttles repeats.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) sharedViewModel.onEvent(SharedEvent.RefreshIfStale)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (sharedState.responseState) {
        is ResponseState.Loading -> {
            HomeLoading()
        }

        is ResponseState.Success -> {
            val data =
                (sharedState.responseState as ResponseState.Success<BaseResponse<DashboardResponse>>).data.apiData
            PullToRefreshBox(
                isRefreshing = sharedState.isRefreshing,
                onRefresh = { sharedViewModel.onEvent(SharedEvent.PullRefresh) },
                modifier = Modifier.fillMaxSize()
            ) {
                HomeSuccessData(state, data, viewModel, sharedViewModel, sharedState, navController, context)
            }
        }

        is ResponseState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBackgroundColor)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Couldn't load your coins",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (sharedState.responseState as ResponseState.Error).message,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                GoldGradientButton(
                    label = "Retry",
                    onClick = { sharedViewModel.onEvent(SharedEvent.FetchBalance) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
private fun HomeLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = OnboardingGold1)
    }
}

@Composable
fun AnnouncementBanner(
    title: String,
    description: String,
    buttonText: String = "Learn More",
    onClick: () -> Unit,
    onClose: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF111111),
                        Color(0xFF1A1A1A),
                        Color(0xFF242424)
                    )
                )
            )
    ) {

        AnnouncementGlow()

        AnnouncementContent(
            title = title,
            description = description,
            buttonText = buttonText,
            onClick = onClick
        )

        AnnouncementCloseButton(onClose = onClose)
    }
}

@Composable
private fun BoxScope.AnnouncementGlow() {
    Box(
        modifier = Modifier
            .size(180.dp)
            .align(Alignment.TopEnd)
            .offset(x = 40.dp, y = (-40).dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        OnboardingGold1.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}

@Composable
private fun AnnouncementContent(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        // Badge — natural height
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(OnboardingGold1.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "ANNOUNCEMENT",
                color = OnboardingGold1,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Text block takes the leftover space and shrinks to fit it
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 14.sp,
                    maxFontSize = 22.sp,
                    stepSize = 1.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 11.sp,
                    maxFontSize = 14.sp,
                    stepSize = 1.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Button — natural height, pinned to bottom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(0.5f),
                contentAlignment = Alignment.CenterEnd
            ) {
                GoldGradientButton(label = buttonText, onClick = onClick)
            }
        }
    }
}

@Composable
private fun BoxScope.AnnouncementCloseButton(onClose: () -> Unit) {
    IconButton(
        onClick = onClose,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .size(28.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Dismiss announcement",
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun HomeSuccessData(
    state: HomeState,
    data: DashboardResponse,
    viewModel: HomeViewModel,
    sharedViewModel: WalletSharedViewModel,
    sharedState: SharedState,
    navController: NavController,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = HomeDimens.ScreenPadding, vertical = HomeDimens.ScreenPadding)
            .verticalScroll(rememberScrollState())
    ) {
        if (state.showAnnouncement) {
            AnnouncementBanner(
                title = "Stake USDT & Earn 12% APY",
                description = "Start earning passive rewards instantly with flexible staking.",
                buttonText = "Start Earning",
                onClose = { viewModel.onEvent(HomeEvent.DismissAnnouncement) },
                onClick = { navController.navigate(Screen.BottomNavScreens.Earn) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        BalanceCard(
            state = state,
            data = data,
            sharedState = sharedState,
            viewModel = viewModel,
            sharedViewModel = sharedViewModel,
            navController = navController
        )

        Spacer(modifier = Modifier.height(HomeDimens.SectionSpacing))

        QuickActionsRow(
            navController = navController,
            context = context
        )

        Spacer(modifier = Modifier.height(HomeDimens.SectionSpacing))

        MarketSnapshotCard(data = data)
    }

}

@Composable
private fun BalanceCard(
    state: HomeState,
    data: DashboardResponse,
    sharedState: SharedState,
    viewModel: HomeViewModel,
    sharedViewModel: WalletSharedViewModel,
    navController: NavController
) {
    OutlinedCard(
        border = BorderStroke(width = 1.dp, color = greyButtonBackground),
        colors = CardDefaults.cardColors(containerColor = AppBackgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(
                top = HomeDimens.CardTopBottomPadding,
                start = HomeDimens.CardContentPadding,
                end = HomeDimens.CardContentPadding,
                bottom = HomeDimens.CardTopBottomPadding
            )
        ) {
            BalanceCardHeader(
                state = state,
                data = data,
                sharedState = sharedState,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = (sharedState.selectedCrypto?.formattedBalance ?: "0.0000"),
                style = MaterialTheme.typography.displayMedium.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                    )
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = ("= $" + (sharedState.selectedCrypto?.formattedFiatValue ?: "0.00")),
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))
            BalanceActionButtons(navController = navController)
        }
    }
}

@Composable
private fun BalanceCardHeader(
    state: HomeState,
    data: DashboardResponse,
    sharedState: SharedState,
    viewModel: HomeViewModel,
    sharedViewModel: WalletSharedViewModel
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        ImageFromUrl(
            sharedState.selectedCrypto?.tokenLogo,
            Modifier.size(20.dp)
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
}

@Composable
private fun BalanceActionButtons(navController: NavController) {
    Row(horizontalArrangement = Arrangement.spacedBy(HomeDimens.ButtonSpacing)) {
        GoldGradientButton(
            label = "Send",
            onClick = {
                navController.navigateSingleTop(Screen.AppNavScreens.Pay)
            },
            modifier = Modifier.weight(1f)
        )
        AppGreyButton(
            labelColor = Color.White,
            label = "Receive",
            onClick = {
                navController.navigateSingleTop(Screen.AppNavScreens.Receive)
            },
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
private fun QuickActionsRow(
    navController: NavController,
    context: Context
) {
    val actions = listOf(
        QuickAction(QuickActionLabels.ADD_FUNDS, Icons.Filled.AddCircleOutline),
        QuickAction(QuickActionLabels.PAY_BILLS, Icons.AutoMirrored.Filled.ReceiptLong),
        QuickAction(QuickActionLabels.STAKING, Icons.Filled.StackedLineChart),
        QuickAction(QuickActionLabels.DAILY_REWARDS, Icons.Filled.Redeem),
        QuickAction(QuickActionLabels.GIFT_CARDS, Icons.Filled.CardGiftcard),
        QuickAction(QuickActionLabels.MORE, Icons.Filled.Apps)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        actions.forEach { action ->
            QuickActionItem(
                action = action,
                onClick = { handleQuickActionClick(action, navController, context) },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

private fun handleQuickActionClick(
    action: QuickAction,
    navController: NavController,
    context: Context
) {
    when (action.label) {
        QuickActionLabels.ADD_FUNDS -> {
            navController.navigateSingleTop(Screen.AppNavScreens.Receive)
        }
        QuickActionLabels.STAKING, QuickActionLabels.DAILY_REWARDS, QuickActionLabels.GIFT_CARDS -> {
            navController.navigateSingleTop(Screen.BottomNavScreens.Earn)
        }
        QuickActionLabels.PAY_BILLS -> {
            navController.navigateSingleTop(Screen.AppNavScreens.Pay)
        }
        else -> {
            val intent = Intent(Intent.ACTION_VIEW,
                EXTERNAL_LINK.toUri())
            context.startActivity(intent)
        }
    }
}

@Composable
private fun QuickActionItem(
    action: QuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "quickActionScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.dp,
                    color = greyButtonBackground,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                action.icon,
                contentDescription = action.label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            action.label,
            style = MaterialTheme.typography.labelMedium.copy(color = Color.White),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp,
            modifier = Modifier.heightIn(min = 28.dp) // reserves space for 2 lines
        )
    }
}

@Composable
private fun MarketSnapshotCard(data: DashboardResponse) {
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
            MarketSnapshotHeader()

            HorizontalDivider(color = greyButtonBackground)

            // Asset Rows
            data.marketSnapshot.forEach { asset ->
                AssetRow(asset = asset)
                HorizontalDivider(color = greyButtonBackground)
            }
        }
    }
}

@Composable
private fun MarketSnapshotHeader() {
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
}

@Composable
private fun AssetRow(asset: DashboardResponse.MarketSnapshot) {
    val isPositive = asset.changePct > 0
    val changeColor = if (isPositive) graphGreenColor else sendAmountTextColor

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
                graphColor = changeColor,
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
                    imageVector = if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = if (isPositive) "Price Up" else "Price Down",
                    tint = changeColor,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(if (isPositive) 45f else -45f)
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
