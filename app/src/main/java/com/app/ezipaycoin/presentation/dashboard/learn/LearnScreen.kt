package com.app.ezipaycoin.presentation.dashboard.learn

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.app.ezipaycoin.data.remote.dto.response.LearnItemResponse
import com.app.ezipaycoin.presentation.dashboard.earn.EarnEmpty
import com.app.ezipaycoin.presentation.dashboard.earn.EarnError
import com.app.ezipaycoin.presentation.dashboard.earn.EarnLoading
import com.app.ezipaycoin.presentation.dashboard.earn.Load
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.ezipaycoin.R
import com.app.ezipaycoin.presentation.shared.SharedEvent
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.blueColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import kotlinx.coroutines.flow.filterIsInstance

data class LearnGridItem(val label: String, val icon: Int)
data class SocialIconItem(val name: String, val icon: Int)

@Composable
fun LearnScreen(
    navController: NavController,
    viewModel: LearnViewModel,
    walletSharedViewModel: WalletSharedViewModel
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        walletSharedViewModel.eventFlow
            .filterIsInstance<SharedEvent.OpenUrl>()
            .collect { event ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                context.startActivity(intent)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LessonsSection(
            lessons = state.lessons,
            onOpen = { url -> walletSharedViewModel.onEvent(SharedEvent.OpenUrl(url)) },
            onRetry = viewModel::load
        )

        Spacer(modifier = Modifier.height(24.dp))
        LearnTopicsGrid(walletSharedViewModel)
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
                onClick = { walletSharedViewModel.onOpenLinkClicked("Terms & Conditions") }) {
                Text(
                    text = "Terms & Conditions",
                    style = MaterialTheme.typography.titleSmall.copy(color = blueColor)
                )
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                border = BorderStroke(width = 1.dp, color = Color.Blue),
                shape = RoundedCornerShape(8.dp),
                onClick = { walletSharedViewModel.onOpenLinkClicked("Privacy Policy") }) {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.titleSmall.copy(color = blueColor)
                )
            }

        }

        Spacer(modifier = Modifier.height(24.dp))
        FooterSection(walletSharedViewModel)
        Spacer(modifier = Modifier.height(24.dp))

    }


}

@Composable
fun LearnTopicsGrid(viewModel: WalletSharedViewModel) {
    val topics = listOf(
        LearnGridItem("Tutorial & Video", R.drawable.tutorial_video),
        LearnGridItem("Whitepaper & Tokenomics", R.drawable.whitepaper_ideomics),
        LearnGridItem("Defi Basic Goals", R.drawable.basic_goals),
        LearnGridItem("FAQ & Troubleshoot", R.drawable.faq_troubleshoot),
        LearnGridItem("Searching Best Content", R.drawable.searching_best_protees),
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
            TopicGridItem(item = topic) {
                viewModel.onOpenLinkClicked(it)
            }
        }
    }
}

@Composable
fun TopicGridItem(item: LearnGridItem, onItemSelected: (String) -> Unit) {
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
            .clickable { onItemSelected(item.label) }
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
private fun LessonsSection(
    lessons: Load<List<LearnItemResponse>>,
    onOpen: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Lessons",
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor)
        )
        when (lessons) {
            Load.Loading -> EarnLoading()
            is Load.Failed -> EarnError(lessons.message, onRetry)
            is Load.Ready ->
                if (lessons.data.isEmpty()) EarnEmpty("No lessons yet. New guides will show up here.")
                else lessons.data.forEach { LessonCard(it, onOpen) }
        }
    }
}

@Composable
private fun LessonCard(item: LearnItemResponse, onOpen: (String) -> Unit) {
    val url = item.resourceUrl?.takeIf { it.startsWith("https://") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = greyButtonBackground, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = url != null) { url?.let(onOpen) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor)
            )
            item.subtitle?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimaryColor))
            }
        }
        if (url != null) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Open", tint = TextPrimaryColor)
        }
    }
}

@Composable
fun FooterSection(viewModel: WalletSharedViewModel) {
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
            IconButton(onClick = { viewModel.onOpenLinkClicked(social.name) }) {
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


//@Preview(showBackground = true, backgroundColor = 0xFF000000)
//@Composable
//fun LearnScreenPreview() {
//    EzipayCoinTheme {
//        LearnScreen(
//            navController = rememberNavController(),
//            viewModel<LearnViewModel>(),
//            walletSharedViewModel
//        )
//    }
//}