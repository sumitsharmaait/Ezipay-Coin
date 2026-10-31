package com.app.ezipaycoin.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle

/** Copies a secret to the clipboard, marks it sensitive and clears it again after a delay. */
object SensitiveClipboard {
    const val CLEAR_AFTER_MILLIS = 60_000L

    private val handler = Handler(Looper.getMainLooper())
    private var pendingClear: Runnable? = null

    fun copy(context: Context, text: String, label: String) {
        val app = context.applicationContext
        val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = ClipData.newPlainText(label, text)
        if (Build.VERSION.SDK_INT >= 33) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        clipboard.setPrimaryClip(clip)

        pendingClear?.let { handler.removeCallbacks(it) }
        val clear = Runnable {
            // Only clear our own clip; the user may have copied something else since.
            val current = clipboard.primaryClip
            if (current?.description?.label == label) {
                if (Build.VERSION.SDK_INT >= 28) {
                    clipboard.clearPrimaryClip()
                } else {
                    clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                }
            }
            pendingClear = null
        }
        pendingClear = clear
        handler.postDelayed(clear, CLEAR_AFTER_MILLIS)
    }
}
