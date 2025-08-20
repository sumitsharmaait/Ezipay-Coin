package com.app.ezipaycoin.presentation.seedphraseview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.composables.GradientText
import com.app.ezipaycoin.ui.composables.TopAppBarWithProgressIndicator
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.grey_18
import com.app.ezipaycoin.ui.theme.grey_22
import com.app.ezipaycoin.ui.theme.grey_9

@Composable
fun WriteDownSeedPhraseScreen(
    navController: NavController,
    viewModel: ViewSeedPhraseViewModel
) {

    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppBackgroundColor,
        topBar = {
            TopAppBarWithProgressIndicator(currentStep = 2, totalSteps = 3) {
                navController.popBackStack()
            }
        },
        bottomBar = {
            if (state.isViewed) {
                GoldGradientButton(
                    "Proceed to Confirm",
                    onClick = { navController.navigate(Screen.Auth.SeedPhraseVerify(state.seedWords)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp)
                )
            } else {
                AppGreyButton(
                    labelColor = grey_18,
                    label = "Next",
                    onClick = { viewModel.showSnackbar("Please View Seed Phrases and write down!!") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp)
                )
            }

        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()), // Enable scrolling
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            GradientText(
                "Write Down Your Seed Phrase",
                modifier = Modifier,
                style = MaterialTheme.typography.titleLarge, // Uses gold color from theme
                align = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "This is your seed phrase. Write it down on a paper and keep it in a safe place. You'll be asked to re-enter this phrase (in order) on the next step.",
                style = MaterialTheme.typography.titleMedium.copy(
                    textAlign = TextAlign.Start,
                    color = TextHintColor,
                    fontWeight = FontWeight(400)
                ),
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Seed Phrase Area (Covered or Revealed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .defaultMinSize(minHeight = 300.dp) // Ensure enough height
                    .paint(
                        painterResource(id = R.drawable.view_seed_phrase_background),
                        contentScale = ContentScale.FillBounds
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (state.isViewed) {
                    RevealedSeedPhraseView(seedWords = state.seedWords)
                } else {
                    CoveredSeedPhraseView(onViewClick = { viewModel.onEvent(SeedPhraseEvent.viewPhrases) })
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

        }
    }
}


@Composable
fun CoveredSeedPhraseView(onViewClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            "Tap to reveal your seed phrase",
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimaryColor,
                fontWeight = FontWeight(600)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Ensure no one can see your screen.",
            style = MaterialTheme.typography.titleSmall.copy(color = grey_9),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onViewClick,
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = greyButtonBackground,
                contentColor = TextPrimaryColor
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Visibility,
                contentDescription = "View Seed Phrase",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("View", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun RevealedSeedPhraseView(seedWords: List<String>) {
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
    }
}

@Composable
fun SeedWordChip(number: Int, word: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(grey_22, RoundedCornerShape(8.dp))
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$number. $word",
            modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 10.dp),
            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor),
        )
    }
}





