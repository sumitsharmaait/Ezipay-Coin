package com.app.ezipaycoin.ui

import androidx.compose.ui.test.assertIsDisplayed
import com.app.ezipaycoin.support.onAllNodesWithTextCount
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.ezipaycoin.presentation.unlock.UnlockScreen
import com.app.ezipaycoin.presentation.unlock.UnlockScreenVM
import com.app.ezipaycoin.support.FakeAuthRepository
import com.app.ezipaycoin.support.ScreenTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class UnlockScreenTest : ScreenTest() {
    private val unlocked = AtomicBoolean(false)

    private fun open(): UnlockScreenVM {
        val vm = onMain { UnlockScreenVM(FakeAuthRepository()) }
        show { UnlockScreen(onUnlocked = { unlocked.set(true) }, viewModel = vm) }
        composeRule.waitUntil(5_000) { vm.dataStoreData != null }
        return vm
    }

    @Test
    fun showsTitlePasswordFieldAndUnlockButton() {
        seed { it.copy(password = "secret-pass") }
        open()
        composeRule.onNodeWithText("Unlock App").assertIsDisplayed()
        composeRule.onNodeWithText("Enter Password").assertIsDisplayed()
        composeRule.onNodeWithText("Unlock").assertIsDisplayed()
    }

    @Test
    fun wrongPasswordShowsAnErrorAndStaysLocked() {
        seed { it.copy(password = "secret-pass") }
        open()
        composeRule.onNodeWithText("Enter Password").performTextInput("not-the-password")
        composeRule.onNodeWithText("Unlock").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTextCount("Incorrect password") > 0
        }
        composeRule.onNodeWithText("Incorrect password").assertIsDisplayed()
        assertFalse(unlocked.get())
    }

    @Test
    fun correctPasswordUnlocksWhenNoWalletIsSetUpYet() {
        // Password saved but wallet setup unfinished: unlock hands control back to the setup flow, no network.
        seed { it.copy(password = "secret-pass", isWalletCreated = false) }
        open()
        composeRule.onNodeWithText("Enter Password").performTextInput("secret-pass")
        composeRule.onNodeWithText("Unlock").performClick()
        composeRule.waitUntil(5_000) { unlocked.get() }
        assertTrue(unlocked.get())
    }

    @Test
    fun passwordFieldCanBeRevealedAndHasAnAccessibleToggle() {
        seed { it.copy(password = "secret-pass") }
        open()
        composeRule.onNodeWithContentDescription("Show password").assertIsDisplayed().performClick()
        composeRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
    }
}
