package com.app.ezipaycoin.presentation.kyc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.ezipaycoin.data.remote.dto.response.KycStatus
import com.app.ezipaycoin.data.remote.dto.response.KycStatusResponse
import com.app.ezipaycoin.ui.composables.Dialogue
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.graphGreenColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.ui.theme.sendAmountTextColor
import com.app.ezipaycoin.utils.ResponseState

private object KycDimens {
    val ScreenPadding = 16.dp
    val CardContentPadding = 24.dp
    val SectionSpacing = 20.dp
    val IconBadgeSize = 96.dp
    val IconGlowSize = 140.dp
    val StepIconSize = 36.dp
}

private val SuccessGreen = graphGreenColor
private val RejectRed = sendAmountTextColor

@Composable
fun KycStatusScreen(
    navController: NavController,
    viewModel: KycViewModel,
    onStartVerification: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when (val res = state.statusResponse) {
            is ResponseState.Loading -> KycLoading()

            is ResponseState.Success -> KycStatusContent(
                data = res.data,
                onStartVerification = onStartVerification
            )

            is ResponseState.Error -> Dialogue(isError = true, msg = res.message) {
                viewModel.onEvent(KycEvent.DismissDialog(0))
            }

            ResponseState.Idle -> Unit
        }
    }
}

@Composable
private fun KycLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = OnboardingGold1)
    }
}

@Composable
private fun KycStatusContent(
    data: KycStatusResponse,
    onStartVerification: () -> Unit
) {
    val ui = rememberStatusUi(data)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(KycDimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StatusBadge(icon = ui.icon, tint = ui.tint)

        Spacer(Modifier.height(KycDimens.SectionSpacing))

        StatusCard(ui = ui)

        if (ui.showSteps) {
            Spacer(Modifier.height(KycDimens.SectionSpacing))
            VerificationStepsCard()
        }

        if (ui.showCta) {
            Spacer(Modifier.height(KycDimens.SectionSpacing))
            GoldGradientButton(
                label = ui.ctaLabel,
                onClick = onStartVerification,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatusBadge(icon: ImageVector, tint: Color) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "kycBadgeScale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(KycDimens.IconGlowSize)
                .background(
                    Brush.radialGradient(
                        colors = listOf(tint.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .size(KycDimens.IconBadgeSize)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(52.dp)
            )
        }
    }
}

@Composable
private fun StatusCard(ui: StatusUi) {
    OutlinedCard(
        border = BorderStroke(width = 1.dp, color = greyButtonBackground),
        colors = CardDefaults.cardColors(containerColor = AppBackgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KycDimens.CardContentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusPill(label = ui.pillLabel, tint = ui.tint)

            Spacer(Modifier.height(16.dp))

            Text(
                text = ui.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    brush = Brush.linearGradient(
                        listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
                    ),
                    fontWeight = FontWeight(800)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = ui.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatusPill(label: String, tint: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(tint.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
private fun VerificationStepsCard() {
    val steps = listOf(
        "Confirm your details" to "Name, date of birth, and address.",
        "Scan a valid ID" to "Passport, driver's licence, or national ID.",
        "Take a quick selfie" to "Used to match you to your document."
    )

    OutlinedCard(
        border = BorderStroke(width = 1.dp, color = greyButtonBackground),
        colors = CardDefaults.cardColors(containerColor = AppBackgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(KycDimens.CardContentPadding)) {
            Text(
                text = "What you'll need",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimaryColor,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(16.dp))
            steps.forEachIndexed { index, (title, detail) ->
                StepRow(number = index + 1, title = title, detail = detail)
                if (index != steps.lastIndex) Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StepRow(number: Int, title: String, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(KycDimens.StepIconSize)
                .clip(CircleShape)
                .background(OnboardingGold1.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = OnboardingGold1,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(Modifier.size(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimaryColor,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
private fun rememberStatusUi(data: KycStatusResponse): StatusUi = when (data.status) {
    KycStatus.APPROVED -> StatusUi(
        icon = Icons.Filled.CheckCircle,
        tint = SuccessGreen,
        pillLabel = "VERIFIED",
        title = "You're verified",
        subtitle = "Your identity is confirmed. All wallet features are unlocked.",
        showCta = false,
        showSteps = false,
        ctaLabel = ""
    )

    KycStatus.PENDING -> StatusUi(
        icon = Icons.Filled.HourglassEmpty,
        tint = OnboardingGold1,
        pillLabel = "UNDER REVIEW",
        title = "Verification in progress",
        subtitle = "We're reviewing your documents. This usually takes only a few minutes.",
        showCta = false,
        showSteps = false,
        ctaLabel = ""
    )

    KycStatus.REJECTED -> StatusUi(
        icon = Icons.Filled.Cancel,
        tint = RejectRed,
        pillLabel = "ACTION NEEDED",
        title = "Verification failed",
        subtitle = data.rejectionReason
            ?: "We couldn't verify your submission. Check your details and try again.",
        showCta = true,
        showSteps = true,
        ctaLabel = "Try again"
    )

    else -> StatusUi(
        icon = Icons.Filled.ShieldMoon,
        tint = OnboardingGold1,
        pillLabel = "NOT VERIFIED",
        title = "Verify your identity",
        subtitle = "Complete a quick check to unlock sending, staking, and rewards.",
        showCta = true,
        showSteps = true,
        ctaLabel = "Start verification"
    )
}

// Holder describing how each KYC state should present.
private data class StatusUi(
    val icon: ImageVector,
    val tint: Color,
    val pillLabel: String,
    val title: String,
    val subtitle: String,
    val showCta: Boolean,
    val showSteps: Boolean,
    val ctaLabel: String
)
