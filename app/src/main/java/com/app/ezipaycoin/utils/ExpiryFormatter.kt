package com.app.ezipaycoin.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object ExpiryFormatter {

    fun formatExpiry(
        oldValue: TextFieldValue,
        newValue: TextFieldValue
    ): TextFieldValue {

        val newDigits = newValue.text.filter { it.isDigit() }
        val oldDigits = oldValue.text.filter { it.isDigit() }

        // 🚫 Block input beyond MM/YY (4 digits)
        if (oldDigits.length == 4 && newDigits.length > oldDigits.length) {
            return oldValue
        }

        val digits = newDigits.take(4)

        // Month validation (only when at least 2 digits entered)
        if (digits.length >= 2) {
            val month = digits.substring(0, 2).toIntOrNull()
            if (month == null || month !in 1..12) {
                return oldValue
            }
        }

        // Count digits before cursor
        var digitsBeforeCursor = 0
        for (i in 0 until newValue.selection.start.coerceAtMost(newValue.text.length)) {
            if (newValue.text[i].isDigit()) {
                digitsBeforeCursor++
            }
        }

        val formatted = StringBuilder()
        var newCursor = 0
        var digitIndex = 0

        digits.forEachIndexed { index, c ->
            formatted.append(c)
            digitIndex++

            if (digitIndex == digitsBeforeCursor) {
                newCursor = formatted.length
            }

            // Add slash after MM
            if (index == 1) {
                formatted.append('/')
                if (digitIndex < digitsBeforeCursor) {
                    newCursor++
                }
            }
        }

        if (digitsBeforeCursor >= digits.length) {
            newCursor = formatted.length
        }

        return TextFieldValue(
            text = formatted.toString(),
            selection = TextRange(newCursor)
        )
    }
}