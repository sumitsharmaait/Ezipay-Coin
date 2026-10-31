package com.app.ezipaycoin.presentation.nft.mint

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.ImageFromUrl
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.cardBorderColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.grey_23
import com.app.ezipaycoin.ui.theme.grey_9
import com.app.ezipaycoin.ui.theme.sendAmountTextColor
import com.app.ezipaycoin.utils.ObserveAsEvents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NftMintScreen(
    viewModel: NftMintViewModel,
    onBack: () -> Unit,
    onMinted: (String?) -> Unit
) {
    val state by viewModel.state.collectAsState()

    ObserveAsEvents(viewModel.navEvents) { event ->
        when (event) {
            NftMintNavEvent.Back -> onBack()
            is NftMintNavEvent.ToHoldings -> onMinted(event.transactionHash)
        }
    }

    if (state.transactionHash != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(NftMintEvent.SuccessAcknowledged) },
            containerColor = grey_23,
            titleContentColor = TextPrimaryColor,
            textContentColor = grey_9,
            title = { Text("Mint submitted") },
            text = {
                Text(
                    "Your NFT is on its way. It'll appear in your wallet once the " +
                        "transaction confirms."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(NftMintEvent.SuccessAcknowledged) }) {
                    Text("View my NFTs", color = Color.White)
                }
            }
        )
    }

    Scaffold(
        containerColor = AppBackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mint",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackgroundColor
                ),
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(NftMintEvent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackgroundColor)
                .padding(padding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )

                // A failed collection load must win over the sale-phase fallback:
                // SalePhase.from(null) resolves to CLOSED, which would otherwise
                // report "minting is closed" for what is actually a server error.
                state.collection == null -> LoadFailed(
                    message = state.error ?: "The collection couldn't be loaded.",
                    onRetry = { viewModel.onEvent(NftMintEvent.Load) }
                )

                else -> MintContent(state = state, onEvent = viewModel::onEvent)
            }
        }
    }
}

@Composable
private fun LoadFailed(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Can't reach the collection",
            style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimaryColor),
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.titleSmall.copy(color = grey_9),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppGreyButton(
            labelColor = Color.White,
            label = "Try again",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MintContent(
    state: NftMintState,
    onEvent: (NftMintEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.medium)
                .background(greyButtonBackground),
            contentAlignment = Alignment.Center
        ) {
            ImageFromUrl(
                state.collection?.imageUrl,
                Modifier.size(200.dp)
            )
        }

        Text(
            text = state.collection?.name ?: "Ezipay Collection",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
        )

        state.remainingSupplyLabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall.copy(color = grey_9)
            )
        }

        state.collection?.description?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall.copy(color = grey_9)
            )
        }

        when (state.phase) {
            SalePhase.CLOSED -> Text(
                text = "Minting is closed right now. Check back when the next drop opens.",
                style = MaterialTheme.typography.titleSmall.copy(color = grey_9),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )

            SalePhase.ALLOWLIST, SalePhase.PUBLIC -> {
                if (state.phase == SalePhase.ALLOWLIST) {
                    Text(
                        text = "Allowlist only",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                    )
                }

                QuantityStepper(state = state, onEvent = onEvent)

                PriceRow("Price each", state.unitPriceLabel)
                PriceRow("Total", state.totalPriceLabel, emphasise = true)

                AppGreyButton(
                    labelColor = Color.White,
                    label = if (state.isMinting) "Minting..." else "Mint ${state.quantity}",
                    onClick = { onEvent(NftMintEvent.MintClicked) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        state.error?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.titleSmall.copy(color = sendAmountTextColor),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuantityStepper(
    state: NftMintState,
    onEvent: (NftMintEvent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Quantity",
            style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimaryColor),
            modifier = Modifier.weight(1f)
        )

        StepperButton(
            symbol = "-",
            enabled = state.canDecrement,
            onClick = { onEvent(NftMintEvent.DecrementQuantity) }
        )

        Text(
            text = state.quantity.toString(),
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
        )

        StepperButton(
            symbol = "+",
            enabled = state.canIncrement,
            onClick = { onEvent(NftMintEvent.IncrementQuantity) }
        )
    }
}

@Composable
private fun StepperButton(
    symbol: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        border = BorderStroke(width = 1.dp, color = cardBorderColor),
        colors = CardDefaults.cardColors(containerColor = grey_23)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = if (enabled) Color.White else grey_9
                )
            )
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    emphasise: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(color = grey_9),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = if (emphasise) {
                MaterialTheme.typography.titleMedium.copy(color = Color.White)
            } else {
                MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
            }
        )
    }
}
