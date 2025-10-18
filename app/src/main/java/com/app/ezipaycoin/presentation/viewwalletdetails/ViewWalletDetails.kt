package com.app.ezipaycoin.presentation.viewwalletdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.presentation.seedphraseview.SeedWordChip
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.grey_9
import com.app.ezipaycoin.utils.copyToClipboard


@Composable
fun ViewWalletDetails(
    navController: NavController,
    vm: ViewWalletDetailsVM
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = "Private Key",
            style = MaterialTheme.typography.titleMedium.copy(
                textAlign = TextAlign.Start,
                color = TextPrimaryColor,
                fontWeight = FontWeight(400)
            ),
        )


        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, grey_9, RoundedCornerShape(8.dp))
                .background(color = AppBackgroundColor)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = state.privateKey,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textAlign = TextAlign.Center,
                        color = TextPrimaryColor,
                        fontWeight = FontWeight(400)
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))

                AppGreyButton(
                    labelColor = TextPrimaryColor,
                    label = "Copy",
                    onClick = {
                         context.copyToClipboard(state.privateKey, "Private Key")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                )
            }

        }
        Spacer(modifier = Modifier.height(32.dp))


        Text(
            text = "Seed phrase",
            style = MaterialTheme.typography.titleMedium.copy(
                textAlign = TextAlign.Start,
                color = TextPrimaryColor,
                fontWeight = FontWeight(400)
            ),
        )


        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .aspectRatio(1f)
                .defaultMinSize(minHeight = 300.dp)
                .paint(
                    painterResource(id = R.drawable.view_seed_phrase_background),
                    contentScale = ContentScale.FillBounds
                ),
            contentAlignment = Alignment.Center
        ) {
            RevealedSeedPhraseView(seedWords = state.seedPhrase.split(" "), onCopy = {
                context.copyToClipboard(state.seedPhrase, "Seed Phrase")
            })
        }


    }

}

@Composable
private fun RevealedSeedPhraseView(seedWords: List<String>, onCopy: () -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .border(1.dp, grey_9, RoundedCornerShape(8.dp))
            .background(color = AppBackgroundColor)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(seedWords) { index, word ->
            SeedWordChip(number = index + 1, word = word)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AppGreyButton(
                    labelColor = TextPrimaryColor,
                    label = "Copy",
                    onClick = {
                        onCopy()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}
