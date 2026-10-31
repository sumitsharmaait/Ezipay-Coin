package com.app.ezipaycoin.support

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText

fun SemanticsNodeInteractionsProvider.onAllNodesWithTextCount(text: String, substring: Boolean = false): Int =
    onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().size

fun SemanticsNodeInteractionsProvider.onAllNodesWithContentDescriptionCount(description: String): Int =
    onAllNodesWithContentDescription(description).fetchSemanticsNodes().size
