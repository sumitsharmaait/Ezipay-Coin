package com.app.ezipaycoin.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object CardFormatter {

    fun formatCardNumber(
        oldValue: TextFieldValue,
        newValue: TextFieldValue
    ): TextFieldValue {

        val newDigits = newValue.text.filter { it.isDigit() }
        val oldDigits = oldValue.text.filter { it.isDigit() }

        // 🚫 HARD BLOCK: ignore input if already at 16 digits
        if (oldDigits.length == 16 && newDigits.length > oldDigits.length) {
            return oldValue
        }

        val digits = newDigits.take(16)

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

            if ((index + 1) % 4 == 0 && index != digits.lastIndex) {
                formatted.append(' ')
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