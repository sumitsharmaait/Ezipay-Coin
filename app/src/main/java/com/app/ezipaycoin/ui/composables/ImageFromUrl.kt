package com.app.ezipaycoin.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.app.ezipaycoin.R
import com.app.ezipaycoin.ui.theme.OnboardingGold1

@Composable
fun ImageFromUrl(
    logoUrl: String?,
    modifier: Modifier,
    progressModifier: Modifier = Modifier
) {
    SubcomposeAsyncImage(
        model = logoUrl,
        contentDescription = "EPAY Logo",
        contentScale = ContentScale.Crop,
        modifier = modifier
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier = progressModifier
                        .size(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = progressModifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = OnboardingGold1
                    )
                }
            }

            is AsyncImagePainter.State.Error -> {
                Image(
                    painter = painterResource(id = R.drawable.ic_ezipay_coin_small),
                    contentDescription = "Fallback logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(20.dp)
                )
            }

            else -> {
                SubcomposeAsyncImageContent() // when successfully loaded
            }
        }
    }
}