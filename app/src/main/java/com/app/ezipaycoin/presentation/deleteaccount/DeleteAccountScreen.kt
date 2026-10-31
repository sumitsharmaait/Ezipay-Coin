package com.app.ezipaycoin.presentation.deleteaccount

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.presentation.main.MainActivity
import com.app.ezipaycoin.ui.composables.AppGreyButton
import com.app.ezipaycoin.ui.composables.AppTextField
import com.app.ezipaycoin.ui.theme.TextPrimaryColor

@Composable
fun DeleteAccountScreen(viewModel: DeleteAccountViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var confirming by remember { mutableStateOf(false) }

    // After a successful deletion the app restarts into onboarding, like a fresh install.
    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            val intent = Intent(context, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
            context.startActivity(intent)
            (context as? Activity)?.finish()
            Runtime.getRuntime().exit(0)
        }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Delete your account?") },
            text = { Text("This cannot be undone. Your wallet on the blockchain is not deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    confirming = false
                    viewModel.confirmDelete()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text("Cancel") } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Delete account",
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimaryColor)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Deleting your account removes your profile, e-mail, profile picture, notifications and " +
                    "sessions from our servers, and removes the wallet from this device.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryColor)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Your wallet on the blockchain is NOT deleted and cannot be. Make sure your recovery " +
                    "phrase is backed up (Profile, Security) before you continue. Without it you lose " +
                    "access to your funds.",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error)
        )
        Spacer(modifier = Modifier.height(24.dp))
        AppTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Enter your password to confirm",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        if (state.error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        AppGreyButton(
            labelColor = MaterialTheme.colorScheme.error,
            label = if (state.loading) "Deleting..." else "Delete my account",
            onClick = { if (state.password.isNotEmpty() && !state.loading) confirming = true },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
