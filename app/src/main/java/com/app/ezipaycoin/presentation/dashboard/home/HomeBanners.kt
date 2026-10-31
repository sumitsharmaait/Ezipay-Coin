package com.app.ezipaycoin.presentation.dashboard.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.AsyncImage
import com.app.ezipaycoin.config.BannerPolicy
import com.app.ezipaycoin.config.BannerTarget
import com.app.ezipaycoin.config.FeatureFlags
import com.app.ezipaycoin.data.remote.dto.response.AppBanner
import com.app.ezipaycoin.navigation.Screen
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import java.time.Instant

/** Promotions from `App/config.banners`. Draws nothing when the server sends none. */
@Composable
fun HomeBanners(navController: NavController) {
    val all by FeatureFlags.banners.collectAsState()
    val enabled by FeatureFlags.enabled.collectAsState()
    var dismissed by rememberSaveable { mutableStateOf(emptyList<Int>()) }

    val shown = BannerPolicy.select(all, Instant.now()).filter { it.id !in dismissed }
    if (shown.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 16.dp)) {
        shown.forEach { banner ->
            val target = BannerPolicy.resolveCta(banner, enabled)
            BannerCard(
                banner = banner,
                onCta = target?.let { { navController.navigateTo(it) } },
                onDismiss = { dismissed = dismissed + banner.id }
            )
        }
    }
}

@Composable
private fun BannerCard(banner: AppBanner, onCta: (() -> Unit)?, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(greyButtonBackground)
    ) {
        BannerPolicy.safeImageUrl(banner)?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = banner.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(banner.title.orEmpty(), style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryColor))
            banner.body?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimaryColor))
            }
            Box(Modifier.fillMaxWidth()) {
                if (onCta != null) {
                    TextButton(onClick = onCta) {
                        Text(banner.ctaLabel.orEmpty(), color = GoldAccentColor)
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterEnd)) {
                    Text("Dismiss", color = TextHintColor)
                }
            }
        }
    }
}

private fun NavController.navigateTo(target: BannerTarget) {
    val route: Any = when (target) {
        BannerTarget.HOME -> Screen.BottomNavScreens.Home
        BannerTarget.WALLET -> Screen.BottomNavScreens.Wallet
        BannerTarget.PAY -> Screen.AppNavScreens.Pay
        BannerTarget.RECEIVE -> Screen.AppNavScreens.Receive
        BannerTarget.HISTORY -> Screen.AppNavScreens.Transactions
        BannerTarget.EARN -> Screen.BottomNavScreens.Earn
        BannerTarget.LEARN -> Screen.BottomNavScreens.Learn
        BannerTarget.PROFILE -> Screen.AppNavScreens.MyProfile
    }
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
