package com.app.ezipaycoin.presentation.dashboard.earn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.R
import com.app.ezipaycoin.config.Feature
import com.app.ezipaycoin.config.FeatureFlags
import com.app.ezipaycoin.presentation.dashboard.earn.airdrops.Airdrops
import com.app.ezipaycoin.presentation.dashboard.earn.checkin.Checkin
import com.app.ezipaycoin.presentation.dashboard.earn.leaderboard.Leaderboard
import com.app.ezipaycoin.presentation.dashboard.earn.spin.SpinAndWinScreen
import com.app.ezipaycoin.presentation.dashboard.earn.staking.Staking
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import java.util.Locale

private enum class EarnTab(val title: String, val feature: Feature?) {
    CHECKIN("Check-in", Feature.EARN_CHECKIN),
    TASKS("Tasks", Feature.EARN_TASKS),
    STAKING("Staking", Feature.STAKING_POOLS),
    AIRDROPS("Airdrops", Feature.AIRDROPS),
    SPIN("Spin", Feature.SPIN_AND_WIN),
    LEADERBOARD("Leaderboard", Feature.LEADERBOARD),
}

@Composable
fun EarnScreen(viewModel: EarnViewModel) {
    val state by viewModel.state.collectAsState()
    val enabled by FeatureFlags.enabled.collectAsState()
    val tabs = remember(enabled) { EarnTab.entries.filter { it.feature == null || it.feature in enabled } }
    var selected by remember { mutableIntStateOf(0) }
    val index = selected.coerceIn(0, (tabs.size - 1).coerceAtLeast(0))
    val current = tabs.getOrNull(index)

    LaunchedEffect(current) {
        if (current == EarnTab.STAKING) viewModel.loadPoolsOnce()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TotalsCard(state.totals, onRetry = viewModel::refresh)

        state.message?.let { message ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = GoldAccentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.dismissMessage() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (tabs.isEmpty() || current == null) {
            EarnEmpty("Rewards are not available right now.")
            return@Column
        }

        ScrollableTabRow(
            selectedTabIndex = index,
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            contentColor = TextPrimaryColor,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                    height = 2.dp,
                    color = GoldAccentColor
                )
            }
        ) {
            tabs.forEachIndexed { i, tab ->
                Tab(
                    selected = index == i,
                    onClick = { selected = i },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (index == i) GoldAccentColor else TextPrimaryColor,
                                fontWeight = if (index == i) FontWeight(800) else FontWeight(400)
                            ),
                        )
                    }
                )
            }
        }

        when (current) {
            EarnTab.CHECKIN -> Scrolling {
                Checkin(
                    status = state.checkIn,
                    busy = state.checkInBusy,
                    onCheckIn = viewModel::checkIn,
                    onRetry = viewModel::refresh
                )
            }
            EarnTab.TASKS -> Scrolling {
                EarnTasks(
                    tasks = state.tasks,
                    completingTaskId = state.completingTaskId,
                    onOpen = { viewModel.onTaskOpened(it.taskId) },
                    onComplete = viewModel::completeTask,
                    onRetry = viewModel::refresh
                )
            }
            EarnTab.STAKING -> Scrolling { Staking(state.pools, onRetry = viewModel::retryPools) }
            // Developer builds only: these have no backend source.
            EarnTab.AIRDROPS -> Airdrops()
            EarnTab.SPIN -> SpinAndWinScreen()
            EarnTab.LEADERBOARD -> Leaderboard()
        }
    }
}

@Composable
private fun Scrolling(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) { content() }
}

@Composable
private fun TotalsCard(totals: Load<RewardTotals>, onRetry: () -> Unit) {
    OutlinedCard(
        border = BorderStroke(width = 1.dp, color = greyButtonBackground),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_ezipay_coin_small), "Token", Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Total Rewards:",
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            when (totals) {
                Load.Loading -> EarnLoading()
                is Load.Failed -> EarnError(totals.message, onRetry)
                is Load.Ready -> {
                    val t = totals.data
                    Text(
                        text = "${amount(t.total)} ${t.symbol}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            brush = Brush.linearGradient(listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4))
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column {
                            Text("Claimable", style = MaterialTheme.typography.labelMedium.copy(color = TextHintColor))
                            Text(
                                "${amount(t.claimable)} ${t.symbol}",
                                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
                            )
                        }
                        Column {
                            Text("Locked", style = MaterialTheme.typography.labelMedium.copy(color = TextHintColor))
                            Text(
                                "${amount(t.locked)} ${t.symbol}",
                                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun amount(value: Double): String = String.format(Locale.US, "%.4f", value)
