package com.app.ezipaycoin.ui.composables

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun Timer(
    totalSeconds: Int = 3 * 60,
    onFinished: () -> Unit = {}
) {
    var remainingSeconds by rememberSaveable { mutableIntStateOf(totalSeconds) }

    LaunchedEffect(key1 = remainingSeconds) {
        if (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else {
            onFinished()
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    Text(
        text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
        style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor),
        textAlign = TextAlign.Center
    )
}
