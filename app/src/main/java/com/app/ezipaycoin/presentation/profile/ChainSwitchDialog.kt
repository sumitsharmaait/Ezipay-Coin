package com.app.ezipaycoin.presentation.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.config.ChainConfig

/**
 * Debug and staging builds only (ChainConfig.switchable): picks the chain for the next start.
 * [onRestart] is called after a different chain has been stored.
 */
@Composable
fun ChainSwitchDialog(onDismiss: () -> Unit, onRestart: () -> Unit) {
    var selected by remember { mutableLongStateOf(ChainConfig.selected().chainId) }
    val changed = selected != ChainConfig.current.chainId

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Network") },
        text = {
            Column {
                Text("In use: ${ChainConfig.current.label}")
                Spacer(modifier = Modifier.height(8.dp))
                ChainConfig.choices.forEach { chain ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == chain.chainId,
                                onClick = { selected = chain.chainId },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected == chain.chainId, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(chain.label)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Signing, the RPC node and explorer links follow this choice. Balances, history " +
                        "and tokens come from the server, which stays on its own chain. " +
                        "The app restarts to apply a change.",
                    style = MaterialTheme.typography.bodySmall
                )
                if (selected == ChainConfig.mainnet.chainId) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${ChainConfig.mainnet.name} moves real funds.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                ChainConfig.select(selected)
                if (changed) onRestart() else onDismiss()
            }) { Text(if (changed) "Restart" else "OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
