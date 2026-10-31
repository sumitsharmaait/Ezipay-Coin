package com.app.ezipaycoin.presentation.nft

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.data.remote.dto.response.NftItem
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.ImageFromUrl
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.cardBorderColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.grey_23
import com.app.ezipaycoin.ui.theme.grey_9

/**
 * The NFTs segment of WalletScreen.
 *
 * Uses a plain Column of chunked Rows rather than LazyVerticalGrid: WalletScreen's
 * root Column already applies verticalScroll, and a lazy vertical container inside a
 * vertically-scrolling parent throws at measure time.
 *
 * The mint action sits outside the holdings state on purpose — minting must stay
 * reachable when the wallet is locked or the holdings request fails.
 */
@Composable
fun NftHoldingsSection(
    state: NftState,
    onEvent: (NftEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (state.items.isEmpty()) "My NFTs" else "My NFTs (${state.items.size})",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
            )
            Text(
                text = "Refresh",
                style = MaterialTheme.typography.titleSmall.copy(color = grey_9),
                modifier = Modifier.clickable { onEvent(NftEvent.Refresh) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }

            state.error != null -> NftNotice(
                title = "Nothing to show yet",
                body = state.error
            )

            state.items.isEmpty() -> NftNotice(
                title = "No NFTs yet",
                body = "Anything you mint from the Ezipay collection will appear here."
            )

            else -> Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.items.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { nft ->
                            NftGridCard(
                                nft = nft,
                                onClick = { onEvent(NftEvent.NftClicked(nft.tokenId)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Always available, whatever the holdings state.
        AppGreyButton(
            labelColor = Color.White,
            label = "Mint an NFT",
            onClick = { onEvent(NftEvent.MintClicked) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NftGridCard(
    nft: NftItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        border = BorderStroke(width = 1.dp, color = cardBorderColor),
        colors = CardDefaults.cardColors(containerColor = grey_23),
        modifier = modifier,
        onClick = onClick
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(greyButtonBackground),
                contentAlignment = Alignment.Center
            ) {
                ImageFromUrl(
                    nft.imageUrl,
                    Modifier.size(120.dp)
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = nft.name ?: "Ezipay #${nft.tokenId}",
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimaryColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "#${nft.tokenId}",
                    style = MaterialTheme.typography.labelSmall.copy(color = grey_9)
                )
            }
        }
    }
}

@Composable
private fun NftNotice(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(greyButtonBackground, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimaryColor),
            textAlign = TextAlign.Center
        )
        Text(
            text = body,
            style = MaterialTheme.typography.titleSmall.copy(color = grey_9),
            textAlign = TextAlign.Center
        )
    }
}
