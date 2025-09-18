package com.app.ezipaycoin.presentation.transactiondetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.app.ezipaycoin.R
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.DialogBackground
import com.app.ezipaycoin.ui.theme.DialogIconCircleGreen
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.cardBorderColor
import com.app.ezipaycoin.ui.theme.cardGreyTextColor
import com.app.ezipaycoin.ui.theme.receivedAmountBackgroundColor
import com.app.ezipaycoin.ui.theme.receivedAmountTextColor
import com.app.ezipaycoin.ui.theme.sendAmountBackgroundColor
import com.app.ezipaycoin.ui.theme.sendAmountTextColor
import com.app.ezipaycoin.utils.shortenAddress

@Composable
fun TransactionDetailsScreen(
    item: TransactionsResponse.TransactionsItem,
    onExplorerClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Amount", style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextHintColor
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                item.formattedAmount, style = MaterialTheme.typography.headlineSmall.copy(
                    color = TextPrimaryColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (item.type.equals(
                        "send",
                        true
                    )
                ) "Sent" else "Received",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = if (item.type.equals(
                            "send",
                            true
                        )
                    ) sendAmountTextColor else receivedAmountTextColor
                ),
                modifier = Modifier
                    .background(
                        if (item.type.equals(
                                "send",
                                true
                            )
                        ) sendAmountBackgroundColor else receivedAmountBackgroundColor,
                        MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "From",
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimaryColor)
                )
                Text(
                    "To",
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimaryColor)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    item.from.shortenAddress(),
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor)
                )
                Icon(
                    painterResource(id = R.drawable.arrow_forward_circle),
                    "To",
                    tint = GoldAccentColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    item.to.shortenAddress(),
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor),
                    modifier = Modifier
                        .background(
                            color = cardGreyTextColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            DialogBackground,
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        "Checked",
                        tint = DialogIconCircleGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Processing",
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimaryColor)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            DialogBackground,
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        "Checked",
                        tint = DialogIconCircleGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.status, style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimaryColor))
            }

        }

        Spacer(modifier = Modifier.height(24.dp))

        InformationSection(item)

        Spacer(modifier = Modifier.height(32.dp))

        GoldGradientButton(
            label = "View on Blockchain Explorer",
            onClick = onExplorerClick,
            modifier = Modifier.fillMaxWidth()
        )

    }

}

@Composable
private fun InformationSection(item: TransactionsResponse.TransactionsItem) {
    var showTooltip by remember { mutableStateOf(false) }

    Column {
        InfoRow(label = "Nonce", value = "193")
        InfoRow(
            label = "Transaction ID",
            value = item.id.shortenAddress(),
            trailingIcon = {
                IconButton(onClick = { /* TODO: Copy ID */ }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Filled.ContentCopy, "Copy", tint = GoldAccentColor)
                }
            }
        )
        InfoRow(label = "Network", value = item.chain)
        Box {
            InfoRow(
                label = "Total Gas Fee",
                value = "0.000021 BNB",
                trailingIcon = {
                    IconButton(onClick = { showTooltip = true }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, "Info", tint = GoldAccentColor)
                    }
                }
            )
            // Custom Tooltip
            if (showTooltip) {
                Popup(
                    alignment = Alignment.CenterEnd,
                    onDismissRequest = { showTooltip = false }
                ) {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = cardBorderColor),
                        modifier = Modifier.padding(top = 4.dp), // Offset from icon
                    ) {
                        Row(
                            modifier = Modifier.width(200.dp).padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum is simply dummy text.",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimaryColor,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Filled.Close,
                                "Close",
                                tint = GoldAccentColor,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { showTooltip = false })
                        }
                    }
                }
            }
        }
        InfoRow(label = "Time", value = item.formattedDate)
    }
}


@Composable
private fun InfoRow(label: String, value: String, trailingIcon: @Composable (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium.copy(color = TextHintColor))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor))
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}