package com.app.ezipaycoin.ui.composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4

@Composable
fun GradientText(
    text: String,
    modifier: Modifier,
    style: TextStyle,
    align : TextAlign
) {
    Text(
        text = text,
        textAlign = align,
        modifier = modifier,
        style = style.copy(
            brush = Brush.linearGradient(
                colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
            )
        )
    )
}