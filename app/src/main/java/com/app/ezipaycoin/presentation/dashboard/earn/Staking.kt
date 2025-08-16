package com.app.ezipaycoin.presentation.dashboard.earn

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.R
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.cardBorderColor
import com.app.ezipaycoin.ui.theme.cardGreyTextColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.grey_23

@Composable
fun Staking() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            "Active Staking Pools",
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor)
        )

        Spacer(modifier = Modifier.height(4.dp))
        val pools = listOf(
            StakingPool(R.drawable.ic_ezipay_coin_small, "Ezipay Staking", "12.5%", "30 Days"),
            StakingPool(R.drawable.ic_usdt_icon, "USDT Staking", "12.5%", "60 Days"),
            StakingPool(R.drawable.ic_ezipay_coin_small, "Ezipay Staking", "12.5%", "30 Days"),
            StakingPool(R.drawable.ic_usdt_icon, "USDT Staking", "12.5%", "60 Days")
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            pools.forEach { pool ->
                StakingPoolCard(pool = pool)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Your Stakes",
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor)
        )
        Spacer(modifier = Modifier.height(4.dp))
        val staks = listOf(
            YourStakes("800 EZP", "200 EZP", "31/10/2025"),
            YourStakes("500 EZP", "100 EZP", "20/12/2025"),
            YourStakes("800 EZP", "200 EZP", "31/10/2025"),
            YourStakes("500 EZP", "100 EZP", "20/12/2025")
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            staks.forEach { stake ->
                YourStakesCard(stake = stake)
            }
        }
    }

}

@Composable
private fun StakingPoolCard(pool: StakingPool) {
    OutlinedCard(
        border = BorderStroke(width = 1.dp, color = cardBorderColor),
        colors = CardDefaults.cardColors(containerColor = grey_23),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painterResource(pool.iconRes), pool.name, Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(2.5f)
            ) {
                Text(
                    pool.name,
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimaryColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = cardGreyTextColor,
                                fontWeight = FontWeight(400)
                            )
                        ) {
                            append("JUL: ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = TextPrimaryColor,
                                fontWeight = FontWeight(400)
                            )
                        ) {
                            append("12.5%")
                        }
                    },
                    style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = cardGreyTextColor,
                                fontWeight = FontWeight(400)
                            )
                        ) {
                            append("Lock: ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = TextPrimaryColor,
                                fontWeight = FontWeight(400)
                            )
                        ) {
                            append("60 days")
                        }
                    },
                    style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
                )
            }
            GoldGradientButton(
                label = "Claim",
                onClick = { /*TODO*/ },
                modifier = Modifier.weight(1.5f)
            )

        }

    }
}

@Composable
private fun YourStakesCard(stake: YourStakes) {
    OutlinedCard(
        border = BorderStroke(width = 1.dp, color = cardBorderColor),
        colors = CardDefaults.cardColors(containerColor = grey_23),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Staked: ${stake.staked}",
                    style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.height(16.dp),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greyButtonBackground)
                ) {
                    Text(
                        "Claim",
                        style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
                    )
                }

            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Earned: ${stake.earned}",
                    style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.height(16.dp),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greyButtonBackground)
                ) {
                    Text(
                        "Claim",
                        style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
                    )
                }

            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = cardGreyTextColor,
                            fontWeight = FontWeight(400)
                        )
                    ) {
                        append("Locked Until: ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = TextPrimaryColor,
                            fontWeight = FontWeight(400)
                        )
                    ) {
                        append(stake.lockedUntil)
                    }
                },
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
            )
        }
    }
}


data class StakingPool(
    @DrawableRes val iconRes: Int,
    val name: String,
    val apr: String,
    val lockPeriod: String
)

data class YourStakes(
    val staked: String,
    val earned: String,
    val lockedUntil: String
)

@Preview
@Composable
fun StakingPreview() {
    EzipayCoinTheme {
        Staking()
    }
}