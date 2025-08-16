package com.app.ezipaycoin.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

fun String.shortenAddress(prefixLength: Int = 7, suffixLength: Int = 5): String {
    if (this.length <= prefixLength + suffixLength) return this
    return "${this.take(prefixLength)}...${this.takeLast(suffixLength)}"
}


fun String.generateQrCodeBitmap(size: Int = 512): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 1) // Small margin
    val qrCodeWriter = QRCodeWriter()
    val bitMatrix = qrCodeWriter.encode(this, BarcodeFormat.QR_CODE, size, size, hints)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) Color.Black.hashCode() else Color.White.hashCode()
            )
        }
    }
    return bitmap
}

fun Context.copyToClipboard(text: String, label: String = "label") {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}

fun Context.pasteFromClipboard(): String? {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clipData = clipboard?.primaryClip
    return if (clipData != null && clipData.itemCount > 0) {
        clipData.getItemAt(0).coerceToText(this).toString()
    } else {
        null
    }
}
