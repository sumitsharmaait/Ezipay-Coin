package com.app.ezipaycoin.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Blocks screenshots, screen recording and the recents thumbnail while the calling screen is
 * shown. Use on every screen that displays a seed phrase or private key.
 */
@Composable
fun SecureScreen() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.findActivity()?.window
        val alreadySecure = window != null &&
                (window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            if (!alreadySecure) window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
