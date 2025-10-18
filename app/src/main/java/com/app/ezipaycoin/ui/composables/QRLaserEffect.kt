package com.app.ezipaycoin.ui.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp



@Composable
fun LaserLineEffect() {
    val transition = rememberInfiniteTransition(label = "laserLine")
    val offsetY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val boxHeightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // ✅ Convert pixel value to Dp safely
        val yPosDp = with(density) { (offsetY * boxHeightPx).toDp() }

        Box(
            modifier = Modifier
                .offset(y = yPosDp)
                .fillMaxWidth(0.9f)
                .height(3.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Red.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}