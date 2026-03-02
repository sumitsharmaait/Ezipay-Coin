package com.app.ezipaycoin.presentation.dashboard.earn.leaderboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.ui.composables.GradientText
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.blueColor
import com.app.ezipaycoin.ui.theme.cardBorderColor
import com.app.ezipaycoin.ui.theme.cardGreyTextColor
import com.app.ezipaycoin.ui.theme.grey_23
import com.app.ezipaycoin.ui.theme.tagsTextColor


data class ClaimedAirdrop(
    val initials: String,
    val name: String,
    val amount: String,
    val iconColor: Color
)

@Composable
fun Leaderboard() {

    val claimedAirdrops = listOf(
        ClaimedAirdrop("YS", "Yatch Sui", "527 YS Token", tagsTextColor),
        ClaimedAirdrop("TC", "Telegraph Coin", "950 TC Token", blueColor),
        ClaimedAirdrop("OC", "Octopus Coin", "1080 OC Token", cardGreyTextColor)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                "Leaderboard",
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor)
            )
        }

        items(claimedAirdrops) { airdrop ->
            ClaimedAirdropItem(item = airdrop)
        }


    }


}

@Composable
private fun ClaimedAirdropItem(item: ClaimedAirdrop) {
    OutlinedCard(
        border = BorderStroke(width = 1.dp, color = cardBorderColor),
        colors = CardDefaults.cardColors(containerColor = grey_23),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialsIcon(initials = item.initials, color = item.iconColor)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                item.name,
                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            GradientText(
                text = "16,702",
                modifier = Modifier,
                style = MaterialTheme.typography.titleMedium,
                align = TextAlign.Start
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "EZP",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimaryColor,
                    textAlign = TextAlign.End
                )
            )
        }
    }
}

@Composable
private fun InitialsIcon(initials: String, color: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials,
            style = MaterialTheme.typography.titleSmall.copy(
                color = TextPrimaryColor
            )
        )
    }
}

@Preview
@Composable
fun StakingPreview() {
    EzipayCoinTheme {
        Leaderboard()
    }
}
