package com.app.ezipaycoin.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.GoldAccentColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.TextHintColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground

@Composable
fun GoldGradientButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val goldGradient = Brush.linearGradient(
        colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
    )
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.8f)
            .clip(RoundedCornerShape(80))
            .background(goldGradient)
            .height(ButtonDefaults.MinHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        Text(
            label,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(500))
        )
    }

}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null, // Custom trailing icon if needed
    isPasswordToggleEnabled: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: ((Boolean) -> Unit)? = null,
    isTextArea: Boolean = false,
    minLines: Int = 1
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, greyButtonBackground, RoundedCornerShape(12.dp))
            .then(if (isTextArea) Modifier.defaultMinSize(minHeight = (minLines * 48).dp) else Modifier), // Approximate height
        label = { Text(label, color = TextHintColor) },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        singleLine = !isTextArea,
        minLines = if (isTextArea) minLines else 1,
        colors = TextFieldDefaults.colors(
            focusedTextColor = TextPrimaryColor,
            unfocusedTextColor = TextPrimaryColor,
            disabledTextColor = TextHintColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            cursorColor = GoldAccentColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLabelColor = TextHintColor, // Keep label color consistent
            unfocusedLabelColor = TextHintColor,
        ),
        trailingIcon = if (isPasswordToggleEnabled && onPasswordVisibilityChange != null) {
            {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"
                IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                    Icon(imageVector = image, description, tint = TextHintColor)
                }
            }
        } else trailingIcon
    )
}

@Composable
fun FaceIdToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GoldAccentColor,
                uncheckedThumbColor = Color(0xFFE0E0E0),
                uncheckedTrackColor = Color(0xFF757575), // A bit lighter than text field bg
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            StepProgressIndicator(
                currentStep = currentStep,
                totalSteps = totalSteps,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimaryColor
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppBackgroundColor,
            titleContentColor = TextPrimaryColor // Not directly used as title is custom
        )
    )
}

@Composable
fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = GoldAccentColor,
    inactiveColor: Color = greyButtonBackground,
    activeDotSize: Dp = 8.dp,
    inactiveDotSize: Dp = 6.dp,
    lineThickness: Dp = 2.dp,
    spacing: Dp = 100.dp // Spacing between centers of dots
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center // Center the whole indicator
    ) {
        for (i in 1..totalSteps) {
            val isCurrentOrPast = i <= currentStep
            val dotSize = if (isCurrentOrPast) activeDotSize else inactiveDotSize
            val color = if (isCurrentOrPast) activeColor else inactiveColor

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(color, CircleShape)
            )

            if (i < totalSteps) {
                // Line connecting dots
                Canvas(
                    modifier = Modifier
                        .height(lineThickness)
                        .width(spacing - dotSize) // Adjust width to be between edges of dots
                        .padding(horizontal = (dotSize - inactiveDotSize) / 2) // Center line slightly better if dots are different sizes
                ) {
                    val yCenter = center.y
                    val startX = 0f
                    val endX = size.width
                    val lineColor = if (i < currentStep) activeColor else inactiveColor
                    drawLine(
                        color = lineColor,
                        start = Offset(startX, yCenter),
                        end = Offset(endX, yCenter),
                        strokeWidth = lineThickness.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun AppGreyButton(
    labelColor: Color,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.8f)
            .clip(RoundedCornerShape(80))
            .height(ButtonDefaults.MinHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = greyButtonBackground,
            contentColor = Color.White
        )
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight(500),
                color = labelColor
            )
        )
    }
}