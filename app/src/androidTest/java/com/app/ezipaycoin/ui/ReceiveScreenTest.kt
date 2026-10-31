package com.app.ezipaycoin.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.ezipaycoin.presentation.receive.ReceiveScreen
import com.app.ezipaycoin.presentation.receive.ReceiveViewModel
import com.app.ezipaycoin.support.Sample
import com.app.ezipaycoin.support.ScreenTest
import com.app.ezipaycoin.support.onAllNodesWithContentDescriptionCount
import com.app.ezipaycoin.support.onAllNodesWithTextCount
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReceiveScreenTest : ScreenTest() {
    @Test
    fun showsTheWalletAddressAndItsQrCode() {
        seed { it.copy(walletAddress = Sample.OWN) }
        val vm = onMain { ReceiveViewModel() }
        show { ReceiveScreen(rememberNavController(), vm) }

        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount(Sample.OWN) > 0 }
        composeRule.onNodeWithText(Sample.OWN).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Wallet Address QR Code").assertIsDisplayed()
        composeRule.onNodeWithText("Ezipay Coin Wallet").assertIsDisplayed()
    }

    @Test
    fun copyButtonIsLabelledAndClickable() {
        seed { it.copy(walletAddress = Sample.OWN) }
        val vm = onMain { ReceiveViewModel() }
        show { ReceiveScreen(rememberNavController(), vm) }
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount(Sample.OWN) > 0 }
        composeRule.onNodeWithContentDescription("Copy Address").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun withoutAWalletNoQrCodeIsShown() {
        val vm = onMain { ReceiveViewModel() }
        show { ReceiveScreen(rememberNavController(), vm) }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithContentDescriptionCount("Wallet Address QR Code").let {
            org.junit.Assert.assertEquals(0, it)
        }
    }
}
