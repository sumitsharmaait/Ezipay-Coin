package com.app.ezipaycoin.presentation.dashboard.earn.staking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.data.remote.dto.response.StakingPoolResponse
import com.app.ezipaycoin.presentation.dashboard.earn.EarnEmpty
import com.app.ezipaycoin.presentation.dashboard.earn.EarnError
import com.app.ezipaycoin.presentation.dashboard.earn.EarnLoading
import com.app.ezipaycoin.presentation.dashboard.earn.Load
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import java.util.Locale

/** Read-only list of the pools the server offers. Staking itself is not available in the app yet. */
@Composable
fun Staking(pools: Load<List<StakingPoolResponse>>, onRetry: () -> Unit) {
    when (pools) {
        Load.Loading -> EarnLoading()
        is Load.Failed -> EarnError(pools.message, onRetry)
        is Load.Ready ->
            if (pools.data.isEmpty()) EarnEmpty("No staking pools are open right now.")
            else Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Staking pools", style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor))
                Text(
                    "View only. Staking from the app is coming later.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor)
                )
                pools.data.forEach { PoolCard(it) }
            }
    }
}

@Composable
private fun PoolCard(pool: StakingPoolResponse) {
    OutlinedCard(
        border = BorderStroke(1.dp, greyButtonBackground),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row {
                Text(
                    pool.name ?: "Pool ${pool.poolId}",
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor),
                    modifier = Modifier.weight(1f)
                )
                pool.apr?.let {
                    Text(
                        String.format(Locale.US, "%.2f%% APR", it),
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldAccentColor)
                    )
                }
            }
            val details = listOfNotNull(
                pool.token?.symbol?.let { "Token: $it" },
                pool.lockDays?.let { "Lock: $it days" },
                pool.minStake?.let { "Minimum: $it" },
                if (pool.isActive) null else "Closed"
            ).joinToString("  |  ")
            if (details.isNotEmpty()) {
                Text(details, style = MaterialTheme.typography.bodySmall.copy(color = TextHintColor))
            }
        }
    }
}
