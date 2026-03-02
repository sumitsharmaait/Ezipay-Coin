package com.app.ezipaycoin.presentation.dashboard.earn.spin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.R
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


@Composable
fun SpinAndWinScreen() {

    val animatedRotationAngle = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var winningSliceIndex by remember { mutableIntStateOf(-1) }
    val coroutineScope = rememberCoroutineScope()
    val wheelSlices = remember {
        listOf(
            R.drawable.spin_slice_6,
            R.drawable.spin_slice_5,
            R.drawable.spin_slice_4,
            R.drawable.spin_slice_3,
            R.drawable.spin_slice_2,
            R.drawable.spin_slice_1
        )
    }

    val sliceCount = wheelSlices.size
    val sliceAngle = 360f / sliceCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "Spin & Win",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimaryColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .aspectRatio(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            FortuneWheelCanvas(
                rotationDegrees = animatedRotationAngle.value,
                slices = wheelSlices,
                modifier = Modifier
                    .fillMaxSize(0.8f)
                    .align(Alignment.Center)
            )

            FortuneWheelSelector()

            SpinButton(
                modifier = Modifier.align(Alignment.Center),
                onClick = {
                    if (!isSpinning) {
                        coroutineScope.launch {
                            isSpinning = true
                            winningSliceIndex = -1

                            val randomWinningSlice = (0 until sliceCount).random()

                            // Slice center
                            val sliceCenterAngle =
                                randomWinningSlice * sliceAngle + sliceAngle / 2f

                            // POINTER VISUAL OFFSET (calibrated)
                            val pointerAngle = 0f

                            // Extra rotations
                            val extraRotations = (4..7).random() * 360f

                            // 🔥 KEY FIX: rotate RELATIVE to current value
                            val currentRotation = animatedRotationAngle.value

                            val deltaRotation =
                                extraRotations + pointerAngle - sliceCenterAngle -
                                        (currentRotation % 360f)

                            val finalRotation = currentRotation + deltaRotation

                            animatedRotationAngle.animateTo(
                                targetValue = finalRotation,
                                animationSpec = tween(
                                    durationMillis = 5000,
                                    easing = CubicBezierEasing(0f, 0.5f, 0.5f, 1f)
                                )
                            )

                            // SNAP to avoid float drift
                            animatedRotationAngle.snapTo(
                                finalRotation.roundToInt().toFloat()
                            )

                            winningSliceIndex = randomWinningSlice
                            isSpinning = false
                        }

                    }
                }
            )


        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "Free Spins: 3",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                "25 EZP Coins",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                "Buy More Spins?",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .clickable { /* TODO */ },
                textAlign = TextAlign.End
            )

        }

    }

}


@Composable
fun FortuneWheelCanvas(
    rotationDegrees: Float,
    slices: List<Int>,
    modifier: Modifier = Modifier
) {
    val sliceCount = slices.size
    val sliceAngle = 360f / sliceCount

    Box(
        modifier.rotate(rotationDegrees)
    ) {

        Canvas(modifier = Modifier.matchParentSize()) {

            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            val goldBrush = Brush.verticalGradient(
                colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
            )
            val darkBrush = SolidColor(greyButtonBackground)

            // Draw slices
            for (i in 0 until sliceCount) {
                val start = (i * sliceAngle) - 90f

                drawArc(
                    brush = if (i % 2 == 0) darkBrush else goldBrush,
                    startAngle = start,
                    sweepAngle = sliceAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }

            // Outlines
            drawCircle(
                color = greyButtonBackground,
                radius = radius,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )
        }

        // ⭐ Place icons using Subcompose — NO DRAW CALLS ⭐
        BoxWithConstraints(
            modifier = Modifier.matchParentSize()
        ) {
            val radiusPx = constraints.maxWidth / 2f
            val iconOffsetRadius = radiusPx * 0.62f

            slices.forEachIndexed { index, icon ->

                val centerAngle = (index * sliceAngle) + sliceAngle / 2f - 90f
                val rad = Math.toRadians(centerAngle.toDouble())

                val x = radiusPx + iconOffsetRadius * cos(rad).toFloat()
                val y = radiusPx + iconOffsetRadius * sin(rad).toFloat()

                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = if (index % 2 == 0) Gradient_1 else greyButtonBackground,
                    modifier = Modifier
                        .size(32.dp)
                        .offset { IntOffset(x.toInt() - 16, y.toInt() - 16) } // center icon
                )
            }
        }
    }
}


@Composable
private fun SpinButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize(0.2f)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                )
            )
            .border(2.dp, AppBackgroundColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Disable ripple effect
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SPIN",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(1000))
        )
    }
}

@Composable
fun FortuneWheelSelector() {
    Icon(
        painter = painterResource(id = R.drawable.spin_selector),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier
            .size(38.dp)
            .offset(y = (-6).dp)
    )
}


//@Preview(showBackground = true, widthDp = 390, heightDp = 844)
//@Composable
//fun SpinAndWinScreenPreview() {
//    EzipayCoinTheme {
//        SpinAndWinScreen()
//    }
//}