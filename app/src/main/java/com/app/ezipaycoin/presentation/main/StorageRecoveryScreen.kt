package com.app.ezipaycoin.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.TextPrimaryColor

/** Shown when the encrypted wallet data on this device can no longer be read. */
@Composable
fun StorageRecoveryScreen(onRestoreFromSeed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wallet data cannot be read",
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "The wallet stored on this device could not be unlocked. Your funds are safe on the " +
                    "blockchain. Restore the wallet with your seed phrase to continue. The unreadable " +
                    "data is kept on the device and is not deleted.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryColor),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        GoldGradientButton(
            "Restore from seed",
            onClick = onRestoreFromSeed,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
