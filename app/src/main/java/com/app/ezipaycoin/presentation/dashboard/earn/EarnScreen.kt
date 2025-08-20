package com.app.ezipaycoin.presentation.dashboard.earn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.R
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground

@Composable
fun EarnScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedCard(
            border = BorderStroke(width = 1.dp, color = greyButtonBackground),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
                        painterResource(R.drawable.ic_ezipay_coin_small),
                        "Token",
                        Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Total Rewards:",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "1,056 EZP Token ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                            )
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "(Claimable)",
                        style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GoldGradientButton(
                        label = "Claimable",
                        onClick = {

                        },
                        modifier = Modifier.weight(1f)
                    )
                    AppGreyButton(
                        labelColor = Color.White,
                        label = "Lock",
                        onClick = {

                        },
                        modifier = Modifier
                            .weight(1f)
                    )
                }


            }

        }


        Spacer(modifier = Modifier.height(24.dp))

        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf("Staking", "Airdrops", "Check-in", "Spin", "Leaderboard")

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            contentColor = TextPrimaryColor,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 2.dp,
                    color = GoldAccentColor
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (selectedTabIndex == index) GoldAccentColor else TextPrimaryColor,
                                fontWeight = if (selectedTabIndex == index) FontWeight(800) else FontWeight(
                                    400
                                )
                            ),
                        )
                    }
                )
            }
        }


        when (selectedTabIndex) {
            0 -> Staking()
        }


    }

}

@Preview
@Composable
fun EarnPreview() {
    EzipayCoinTheme {
        EarnScreen()
    }
}