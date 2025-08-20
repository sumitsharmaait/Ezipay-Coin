package com.app.ezipaycoin.presentation.confirmseedphrase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.composables.TopAppBarWithProgressIndicator
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.GoldTextColor
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.grey_18
import com.app.ezipaycoin.ui.theme.grey_22
import com.app.ezipaycoin.ui.theme.grey_23
import com.app.ezipaycoin.ui.theme.grey_9
import com.app.ezipaycoin.utils.ResponseState


@Composable
fun ConfirmSeedPhraseScreen(
    navController: NavController,
    originalSeeds: List<String>,
    viewModel: ConfirmSeedPhraseViewModel
) {

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(key1 = context) {
        viewModel.createDistract(originalSeed = originalSeeds, (1..4).random())
    }

    Scaffold(
        containerColor = AppBackgroundColor,
        topBar = {
            TopAppBarWithProgressIndicator(currentStep = 3, totalSteps = 3) {
                navController.popBackStack()
            }
        },
        bottomBar = {
            if (!state.isAllConfirmed) {
                AppGreyButton(
                    labelColor = grey_18,
                    label = "Next",
                    onClick = { viewModel.showSnackbar("Please Verify Seed First!!") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp)
                )
            } else {
                GoldGradientButton(
                    "Next",
                    onClick = {
                        navController.navigate(Screen.Auth.WalletSuccess)
                    },
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
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Confirm Seed Phrase",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge.copy(color = GoldTextColor) // Uses gold color from theme
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Select the words in the exact order shown earlier",
                style = MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Start,
                    color = TextHintColor,
                    fontWeight = FontWeight(400)
                )
            )
            Spacer(Modifier.weight(0.2f))

            // Large display of current word being confirmed
            Text(
                text = state.currentConfirmationIndex.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(color = GoldTextColor),
                modifier = Modifier.defaultMinSize(minHeight = 60.dp) // Ensure space even if word is short
            )


//            if (currentConfirmationIndex < originalPhrase.size) {
//                Text(
//                    text = "${currentConfirmationIndex + 1}. ${
//                        confirmedWords.getOrNull(
//                            currentConfirmationIndex
//                        ) ?: ""
//                    }",
//                    style = MaterialTheme.typography.headlineMedium,
//                    modifier = Modifier.defaultMinSize(minHeight = 60.dp) // Ensure space even if word is short
//                )
//            } else {
//                Text( // Placeholder or "All Confirmed!"
//                    text = "All Confirmed!",
//                    style = MaterialTheme.typography.headlineMedium.copy(color = GoldTextColor),
//                    modifier = Modifier.defaultMinSize(minHeight = 60.dp)
//                )
//            }


            Spacer(Modifier.weight(0.2f))

            // Word confirmation progress indicator (lines)
            WordConfirmationProgressLines(
                totalWords = 3,
                confirmedCount = if (state.confirmedSeeds.isEmpty()) 0 else state.confirmedSeeds.size // Number of words fully confirmed
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Selectable word chips
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.dp, grey_9, RoundedCornerShape(8.dp))
                    .background(color = AppBackgroundColor)
                    .defaultMinSize(minHeight = 150.dp) // Enough space for 2 rows of chips
                    .clip(MaterialTheme.shapes.medium)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!state.isAllConfirmed) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3), // 3 chips per row
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.distractSeeds) { word ->
                            SelectableWordChip(
                                text = word,
                                onClick = {
                                    viewModel.onEvent(ConfirmSeedPhraseEvent.OnVerifySeedClick(word))
//                                    if (word == originalPhrase.getOrNull(currentConfirmationIndex)) {
//                                        confirmedWords.add(word) // Add to locally displayed confirmed words
//                                        currentConfirmationIndex++
//                                        // In a real app, you might fetch new options here or just rely on 'remember'
//                                    } else {
//                                        // Handle incorrect selection (e.g., show error, reset step)
//                                    }
                                }
                            )
                        }
                    }
                } else {
                    if (state.responseState is ResponseState.Loading) {
                        CircularProgressIndicator(color = OnboardingGold1)
                    }
                    else if (state.responseState is ResponseState.Success) {
                        Text(
                            "Ready to proceed!",
                            style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimaryColor)
                        )
                    }
                    else if (state.responseState is ResponseState.Error) {
                        Text(
                            "Error: ${(state.responseState as ResponseState.Error).message}",
                            color = Color.Red
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

    }
}

@Composable
fun WordConfirmationProgressLines(
    totalWords: Int,
    confirmedCount: Int,
    activeColor: Color = GoldTextColor,
    inactiveColor: Color = grey_23,
    lineHeight: Dp = 4.dp,
    lineWidth: Dp = 25.dp,
    spacing: Dp = 6.dp
) {
    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
        for (i in 0 until totalWords) {
            Box(
                modifier = Modifier
                    .width(lineWidth)
                    .height(lineHeight)
                    .background(
                        if (i < confirmedCount) activeColor else inactiveColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun SelectableWordChip(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium, // Rounded corners for chips
        colors = ButtonDefaults.buttonColors(
            containerColor = grey_22,
            contentColor = TextPrimaryColor
        ),
        modifier = Modifier.defaultMinSize(minWidth = 80.dp), // Ensure chips have decent width
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}




