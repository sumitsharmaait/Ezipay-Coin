package com.app.ezipaycoin.presentation.securewallet


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.composables.GradientText
import com.app.ezipaycoin.ui.composables.TopAppBarWithProgressIndicator
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.ui.theme.GoldTextColor
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.grey_9
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureWallet(
    navController: NavController,
    viewModel: SecureWalletViewModel = viewModel()
) {

    Scaffold(
        modifier = Modifier.then(
            if (viewModel.state.seedPhraseBottomSheetVisible || viewModel.state.secureWalletBottomSheetVisible) {
                Modifier
                    .background(Color.Black.copy(alpha = 0.3f))
                    .blur(radius = 8.dp)
            } else {
                Modifier
            }
        ),
        containerColor = AppBackgroundColor,
        topBar = {
            TopAppBarWithProgressIndicator(currentStep = 2, totalSteps = 3) {
                /* TODO: Handle on back click */
            }
        },
        bottomBar = {
            GoldGradientButton(
                "View My Seed Phrase",
                onClick = { navController.navigate(Screen.Auth.SeedPhrase) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()), // Enable scrolling
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
//                Text(
//                    "Secure Your Wallet",
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier
//                        .weight(1f),
//                    style = MaterialTheme.typography.titleLarge.copy(color = GoldTextColor) // Uses gold color from theme
//                )

                GradientText(
                    text = "Secure Your Wallet",
                    align = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f),
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(onClick = {  viewModel.onEvent(SecureWalletEvent.SecureWalletBottomSheet) }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Scan Seed Phrase",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = TextHintColor,
                            fontWeight = FontWeight(400)
                        )
                    ) {
                        append("Secure your wallet's ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = GoldTextColor,
                            fontWeight = FontWeight(600)
                        )
                    ) {
                        withLink(
                            link = LinkAnnotation.Clickable(
                                tag = "SeedPhrase",
                                linkInteractionListener = {
                                    viewModel.onEvent(SecureWalletEvent.SeedPhraseBottomSheet)
                                },
                            ),
                        ) {
                            append("\"Seed Phrase\"")
                        }
                    }

                },
                style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Start),
                modifier = Modifier
                    .fillMaxWidth(1f),
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionContent(
                title = "Manual",
                items = listOf(
                    "Write down your seed phrase on a piece of paper and store in a safe place."
                ),
                isBulletList = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Security lever: Very strong",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SecurityLeverIndicator()
            Spacer(modifier = Modifier.height(24.dp))

            SectionContent(
                title = "Risks are:",
                items = listOf(
                    "You lose it",
                    "You forget where you put it",
                    "Someone else finds it"
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Other options: Doesn't have to be paper!",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))

            SectionContent(
                title = "Tips:",
                items = listOf(
                    "Store in bank vault",
                    "Store in a safe",
                    "Store in multiple secret places"
                )
            )

            if (viewModel.state.seedPhraseBottomSheetVisible) {
                val sheetState =
                    rememberModalBottomSheetState(skipPartiallyExpanded = true) // Sheet fully expands or hides
                val scope = rememberCoroutineScope()
                ModalBottomSheet(
                    onDismissRequest = { viewModel.onEvent(SecureWalletEvent.DismissSeedPhraseBottomSheet) },
                    sheetState = sheetState,
                    shape = MaterialTheme.shapes.extraLarge, // Rounded top corners
                    containerColor = AppBackgroundColor, // Sheet background color
                    dragHandle = { // Standard drag handle
                        BottomSheetDefaults.DragHandle(
                            color = grey_9,
                            width = 40.dp,
                            height = 4.dp
                        )
                    }
                ) {
                    SeedPhraseInfoSheetContent(
                        onGotItClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    viewModel.onEvent(SecureWalletEvent.DismissSeedPhraseBottomSheet)
                                }
                            }
                            // TODO: Handle "I Got It" action
                        }
                    )
                }
            }

            if (viewModel.state.secureWalletBottomSheetVisible) {
                val sheetState =
                    rememberModalBottomSheetState(skipPartiallyExpanded = true) // Sheet fully expands or hides
                val scope = rememberCoroutineScope()
                ModalBottomSheet(
                    onDismissRequest = { viewModel.onEvent(SecureWalletEvent.DismissSecureWalletBottomSheet) },
                    sheetState = sheetState,
                    shape = MaterialTheme.shapes.extraLarge, // Rounded top corners
                    containerColor = AppBackgroundColor, // Sheet background color
                    dragHandle = { // Standard drag handle
                        BottomSheetDefaults.DragHandle(
                            color = grey_9,
                            width = 40.dp,
                            height = 4.dp
                        )
                    }
                ) {
                    SecureWalletSheetContent(
                        onGotItClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    viewModel.onEvent(SecureWalletEvent.DismissSecureWalletBottomSheet)
                                }
                            }
                            // TODO: Handle "I Got It" action
                        }
                    )
                }
            }


        }
    }
}

@Composable
fun SectionContent(title: String, items: List<String>, isBulletList: Boolean = true) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            if (isBulletList) {
                BulletListItem(text = item)
            } else {
                Text(item, style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            Spacer(modifier = Modifier.height(if (isBulletList) 4.dp else 8.dp))
        }
    }
}

@Composable
fun BulletListItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
            modifier = Modifier.offset(y = (-1).dp) // Slight offset for better alignment
        )
        Text(text = text, style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
    }
}

@Composable
fun SecurityLeverIndicator(
    count: Int = 3,
    lineColor: Color = GoldTextColor,
    lineWidth: Dp = 40.dp,
    lineHeight: Dp = 3.dp,
    spacing: Dp = 8.dp
) {
    Row {
        for (i in 0 until count) {
            Box(
                modifier = Modifier
                    .width(lineWidth)
                    .height(lineHeight)
                    .background(lineColor, shape = RoundedCornerShape(2.dp))
            )
            if (i < count - 1) {
                Spacer(modifier = Modifier.width(spacing))
            }
        }
    }
}


@Composable
fun SeedPhraseInfoSheetContent(onGotItClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 32.dp
            ) // Top padding handled by drag handle space
            .verticalScroll(rememberScrollState()), // In case content is long
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp)) // Space after drag handle

        Text(
            text = "What is a 'Seed phrase'",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "A seed phrase is a set of twelve words that contains all the information about your wallet, including your funds. It's like a secret code used to access your entire wallet.",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
            textAlign = TextAlign.Start // Text paragraphs are usually start-aligned
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You must keep your seed phrase secret and safe. If someone gets your seed phrase, they'll gain control over your accounts.",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Save it in a place where only you can access it. If you lose it, not even MetaMask can help you recover it.",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(32.dp))

        GoldGradientButton(
            label = "I Got It",
            onClick = { onGotItClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 8.dp)
        )

    }
}

@Composable
fun SecureWalletSheetContent(onGotItClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 32.dp
            ) // Top padding handled by drag handle space
            .verticalScroll(rememberScrollState()), // In case content is long
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp)) // Space after drag handle

        Text(
            text = "Protect Your Wallet",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Don't’t risk losing your funds. Protect your wallet by saving your seed phrase in a place you trust.",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
            textAlign = TextAlign.Start // Text paragraphs are usually start-aligned
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "It’s the only way to recover your wallet if you get locked out of the app or get a new device.",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(32.dp))

        GoldGradientButton(
            label = "I Got It",
            onClick = { onGotItClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 8.dp)
        )

    }
}

@Preview
@Composable
fun SecureWalletPreview() {
    EzipayCoinTheme {
        SecureWallet(navController = rememberNavController())
    }
}







