package com.app.ezipaycoin.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.presentation.transactions.AllTransactionsViewModel
import com.app.ezipaycoin.presentation.transactions.TransactionScreen
import com.app.ezipaycoin.support.FakeTransactionsRepository
import com.app.ezipaycoin.support.ScreenTest
import com.app.ezipaycoin.support.onAllNodesWithTextCount
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionsScreenTest : ScreenTest() {
    private val repo = FakeTransactionsRepository()
    private var clicked: TransactionsResponse.TransactionsItem? = null

    private fun open() {
        val vm = onMain { AllTransactionsViewModel(repo) }
        show { TransactionScreen(rememberNavController(), vm) { clicked = it } }
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("-12.5000 EZPT") > 0 }
    }

    @Test
    fun listsEachTransactionWithSignedAmount() {
        open()
        composeRule.onNodeWithText("-12.5000 EZPT").assertIsDisplayed()
        composeRule.onNodeWithText("+0.1000 EZPT").assertIsDisplayed()
        // Both rows show the other side of the transfer: the recipient of the sent one, the sender of the received one.
        composeRule.onAllNodesWithText("0xfB691...5d359").assertCountEquals(2)
        composeRule.onAllNodesWithText("October 1, 2026 | 10:15AM").assertCountEquals(2)
    }

    @Test
    fun showsTheThreeFilters() {
        open()
        composeRule.onNodeWithText("All").assertIsDisplayed()
        composeRule.onNodeWithText("Send").assertIsDisplayed()
        composeRule.onNodeWithText("Received").assertIsDisplayed()
    }

    @Test
    fun choosingAFilterRequestsThatType() {
        open()
        composeRule.onNodeWithText("Received").performClick()
        composeRule.waitUntil(5_000) { synchronized(repo.historyRequests) { repo.historyRequests.any { it.type == "received" } } }
        assertEquals("all", repo.historyRequests.first().type)
    }

    @Test
    fun tappingARowReportsTheTransaction() {
        open()
        composeRule.onNodeWithText("-12.5000 EZPT").performClick()
        composeRule.waitUntil(5_000) { clicked != null }
        assertEquals("1", clicked?.id)
    }
}
