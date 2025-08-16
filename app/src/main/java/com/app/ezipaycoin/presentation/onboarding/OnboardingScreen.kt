package com.app.ezipaycoin.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.data.remote.dto.OnboardingPage
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.composables.GradientText
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.OnboardingGold2
import com.app.ezipaycoin.ui.theme.OnboardingTextDark
import com.app.ezipaycoin.ui.theme.PagerDotActive
import com.app.ezipaycoin.ui.theme.PagerDotInactive
import kotlinx.coroutines.launch


@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.ic_onboarding_property, // Replace with your actual drawable
            titleLine1 = "Multi",
            titleLine2 = "Utility",
            titleLine1Color = OnboardingTextDark,
            titleLine2Color = OnboardingGold1
        ),
        OnboardingPage(
            imageRes = R.drawable.illus, // Replace with your actual drawable
            titleLine1 = "Your Keys",
            titleLine2 = "Your Crypto",
            titleLine1Color = OnboardingTextDark,
            titleLine2Color = OnboardingGold1
        ),
        OnboardingPage(
            imageRes = R.drawable.ic_onboarding_rocket, // Replace with your actual drawable
            titleLine1 = "Speed",
            titleLine2 = "Rewards",
            titleLine1Color = OnboardingGold2,
            titleLine2Color = OnboardingTextDark
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .background(AppBackgroundColor)
            .fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            OnboardingPageItem(page = pages[pageIndex])
        }

        // Skip Button
//        Button(
//            onClick = {
//                // Handle Skip action, e.g., navigate to home
//            },
//            colors = ButtonDefaults.buttonColors(
//                containerColor = greyButtonBackground,
//                contentColor = Color.White
//            ),
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(16.dp)
//                .clip(RoundedCornerShape(80)),
//        ) {
//            Text(
//                "Skip",
//                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight(500))
//            )
//        }

        // Pager Indicator and Bottom Button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PagerIndicator(
                pagerState = pagerState,
                pageCount = pages.size
            )
            Spacer(modifier = Modifier.height(40.dp))

            val isLastPage = pagerState.currentPage == pages.size - 1
            if (isLastPage) {
                GoldGradientButton(
                    label = "Launch Wallet",
                    onClick = { navController.navigate(Screen.Auth.WalletSetup) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp)
                )
            } else {
                AppGreyButton(
                    labelColor = Color.White, label = "Get Start", onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingPageItem(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center content vertically
    ) {
        Spacer(Modifier.weight(0.2f)) // Pushes content down a bit from the very top

        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.titleLine1,
            modifier = Modifier
                .fillMaxWidth(0.7f) // Adjust size as needed
                .aspectRatio(1f), // Maintain aspect ratio, adjust if images are not square
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.titleLine1,
            style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))
        GradientText(
            text = page.titleLine2,
            modifier = Modifier,
            style = MaterialTheme.typography.displaySmall,
            align = TextAlign.Center
        )
//        Text(
//            text = page.titleLine2,
//            style = MaterialTheme.typography.displaySmall.copy(color = GoldTextColor)
//        )

        Spacer(Modifier.weight(0.3f)) // Pushes indicators/buttons towards bottom
    }
}


@Composable
fun PagerIndicator(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = PagerDotActive,
    inactiveColor: Color = PagerDotInactive,
    dotSize: Dp = 8.dp,
    spacing: Dp = 8.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) activeColor else inactiveColor
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}






