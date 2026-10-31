package com.app.ezipaycoin.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.ezipaycoin.data.repository.UserDataRepoImpl
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.presentation.dashboard.home.HomeScreen
import com.app.ezipaycoin.presentation.dashboard.home.HomeViewModel
import com.app.ezipaycoin.presentation.shared.SharedEvent
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.support.FakeHomeRepository
import com.app.ezipaycoin.support.ScreenTest
import com.app.ezipaycoin.support.Sample
import com.app.ezipaycoin.support.onAllNodesWithTextCount
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class HomeScreenTest : ScreenTest() {
    private val repo = FakeHomeRepository()

    private lateinit var shared: WalletSharedViewModel

    private fun open(unlock: Boolean = true) {
        shared = onMain {
            WalletSharedViewModel(repo, UserDataRepoImpl(UserPreferencesRepository.userPreferencesFlow))
        }
        val home = onMain { HomeViewModel(repo) }
        show { HomeScreen(rememberNavController(), home, shared) }
        if (unlock) composeRule.runOnUiThread { shared.onEvent(SharedEvent.AppUnlocked) }
    }

    @Test
    fun showsTheSelectedCoinBalanceAndFiatValue() {
        open()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("1250.5000") > 0 }
        composeRule.onNodeWithText("1250.5000").assertIsDisplayed()
        composeRule.onNodeWithText("= $62.5250").assertIsDisplayed()
        composeRule.onNodeWithText("Est. Total Value").assertIsDisplayed()
        composeRule.onNodeWithText("EZPT").assertIsDisplayed()
    }

    @Test
    fun switchingCoinShowsItsBalance() {
        open()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("1250.5000") > 0 }
        composeRule.onNodeWithText("EZPT").performClick()
        composeRule.onNodeWithText("BNB").performClick()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("0.2500") > 0 }
        composeRule.onNodeWithText("= $150.0000").assertIsDisplayed()
    }

    @Test
    fun pullToRefreshFetchesTheDashboardAgain() {
        open()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("1250.5000") > 0 }
        assertEquals(1, repo.calls.get())

        repo.dashboard = Sample.dashboard.copy(
            crypto = listOf(Sample.coin("EZPT", 1300.0, 65.0), Sample.coin("BNB", 0.25, 150.0))
        )
        composeRule.onRoot().performTouchInput { swipeDown(startY = top + 20f, endY = bottom - 20f, durationMillis = 600) }
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("1300.0000") > 0 }
        assertEquals(2, repo.calls.get())
    }

    @Test
    fun failedLoadShowsTheReasonAndRetryRecovers() {
        repo.failure = IOException("Unable to reach the server")
        open()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("Couldn't load your coins") > 0 }
        composeRule.onNodeWithText("Unable to reach the server").assertIsDisplayed()

        repo.failure = null
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("1250.5000") > 0 }
    }

    @Test
    fun currencyPickerIconIsLabelled() {
        open()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("1250.5000") > 0 }
        composeRule.onNodeWithContentDescription("Select Currency").assertIsDisplayed()
    }
}
