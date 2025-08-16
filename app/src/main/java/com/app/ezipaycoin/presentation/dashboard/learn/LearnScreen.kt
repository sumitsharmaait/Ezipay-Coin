package com.app.ezipaycoin.presentation.dashboard.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.theme.EzipayCoinTheme
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.blueColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground

data class LearnGridItem(val label: String, val icon: Int)
data class SocialIconItem(val name: String, val icon: Int)

@Composable
fun LearnScreen(
    navController: NavController,
    viewModel: LearnViewModel
) {

    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = greyButtonBackground,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Getting started with Ezipay Wallet",
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor)
            )
            Spacer(modifier = Modifier.height(12.dp))
            IconButton(
                onClick = { /* TODO: Play Video */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Gradient_4,
                                Gradient_3,
                                Gradient_2,
                                Gradient_1
                            )
                        ),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Learn how to setup",
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppGreyButton(
                labelColor = TextPrimaryColor,
                label = "Start Now",
                onClick = { /* TODO: Start Now */ },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }


        Spacer(modifier = Modifier.height(24.dp))
        LearnTopicsGrid()
        Spacer(modifier = Modifier.height(24.dp))
        FeaturedTopicCard(
            title = "How to stake Ezipay?",
            subtitle = "Step by Step spacing guide"
        )
        Spacer(modifier = Modifier.height(24.dp))
        NewTutorialCard()
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedButton(
                modifier = Modifier
                    .weight(1f),
                border = BorderStroke(width = 1.dp, color = Color.Blue),
                shape = RoundedCornerShape(8.dp),
                onClick = { /*TODO*/ }) {
                Text(
                    text = "Terms & Conditions",
                    style = MaterialTheme.typography.titleSmall.copy(color = blueColor)
                )
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                border = BorderStroke(width = 1.dp, color = Color.Blue),
                shape = RoundedCornerShape(8.dp),
                onClick = { /*TODO*/ }) {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.titleSmall.copy(color = blueColor)
                )
            }

        }

        Spacer(modifier = Modifier.height(24.dp))
        FooterSection()
        Spacer(modifier = Modifier.height(24.dp))

    }


}

@Composable
fun LearnTopicsGrid() {
    val topics = listOf(
        LearnGridItem("Tutorial & Video", R.drawable.tutorial_video),
        LearnGridItem("White Paper & Ideomics", R.drawable.whitepaper_ideomics),
        LearnGridItem("Defi Basic Goals", R.drawable.basic_goals),
        LearnGridItem("FAQ & Troubleshoot", R.drawable.faq_troubleshoot),
        LearnGridItem("Searching Best Protees", R.drawable.searching_best_protees),
        LearnGridItem("Support & Onboard", R.drawable.support_onboard)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        items(topics) { topic ->
            TopicGridItem(item = topic)
        }
    }
}

@Composable
fun TopicGridItem(item: LearnGridItem) {
    Column(
        modifier = Modifier
            .aspectRatio(0.9f) // Slightly taller than wide
            .border(
                width = 1.dp,
                color = greyButtonBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .background(Color.Transparent, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { /* TODO: Navigate to topic */ }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            painterResource(id = item.icon),
            contentDescription = item.label,
            modifier = Modifier.size(48.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            item.label,
            style = MaterialTheme.typography.titleSmall.copy(
                color = TextPrimaryColor
            ),
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FeaturedTopicCard(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .background(color = greyButtonBackground, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Gradient_4,
                            Gradient_3,
                            Gradient_2,
                            Gradient_1
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = title,
                tint = TextPrimaryColor
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor),
                textAlign = TextAlign.Center
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimaryColor),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = { /* TODO: Play Video */ },
            modifier = Modifier
                .size(36.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Gradient_4,
                            Gradient_3,
                            Gradient_2,
                            Gradient_1
                        )
                    ),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Outlined.PlayArrow,
                "Play",
                tint = TextPrimaryColor
            )
        }
    }
}

@Composable
fun NewTutorialCard() {
    Column(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = greyButtonBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "New: Ezipay mobile tutorial",
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor)
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppGreyButton(
            labelColor = TextPrimaryColor,
            label = "Watch Now",
            onClick = { /* TODO: Watch Now */ },
            modifier = Modifier
                .fillMaxWidth(),
        )
    }
}

@Composable
fun FooterSection() {
    val socialIcons = listOf(
        SocialIconItem("Facebook", R.drawable.round_facebook),
        SocialIconItem("X", R.drawable.round_twitter), // Placeholder
        SocialIconItem("LinkedIn", R.drawable.round_linkedin), // Placeholder
        SocialIconItem("Instagram", R.drawable.round_instagram), // Placeholder
        SocialIconItem("YouTube", R.drawable.round_youtube)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        socialIcons.forEach { social ->
            IconButton(onClick = { /* TODO: Open social link */ }) {
                Icon(
                    painter = painterResource(id = social.icon),
                    contentDescription = social.name,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun LearnScreenPreview() {
    EzipayCoinTheme {
        LearnScreen(navController = rememberNavController(), viewModel<LearnViewModel>())
    }
}