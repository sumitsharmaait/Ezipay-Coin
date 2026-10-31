package com.app.ezipaycoin.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.data.repository.UserDataRepoImpl
import com.app.ezipaycoin.presentation.dashboard.pay.PayScreen
import com.app.ezipaycoin.presentation.dashboard.pay.PayViewModel
import com.app.ezipaycoin.presentation.shared.SharedEvent
import com.app.ezipaycoin.presentation.shared.WalletSharedViewModel
import com.app.ezipaycoin.support.FakeHomeRepository
import com.app.ezipaycoin.support.FakeTransactionsRepository
import com.app.ezipaycoin.support.Sample
import com.app.ezipaycoin.support.ScreenTest
import com.app.ezipaycoin.support.onAllNodesWithTextCount
import com.app.ezipaycoin.utils.validation.AddressValidator
import com.app.ezipaycoin.utils.validation.AmountValidator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Send flow through the real PayScreen and PayViewModel with a fake chain. Signing is never reached. */
@RunWith(AndroidJUnit4::class)
class SendScreenTest : ScreenTest() {
    private val chain = FakeTransactionsRepository()
    private val home = FakeHomeRepository()

    private fun open(withWallet: Boolean = true) {
        if (withWallet) seed { it.copy(walletAddress = Sample.OWN, walletPrivateKey = Sample.FAKE_KEY, isWalletCreated = true) }
        val shared = onMain { WalletSharedViewModel(home, UserDataRepoImpl(UserPreferencesRepository.userPreferencesFlow)) }
        val pay = onMain { PayViewModel(chain) }
        show { PayScreen(rememberNavController(), pay, shared, onClickViewAllTransactions = {}) }
        composeRule.runOnUiThread { shared.onEvent(SharedEvent.AppUnlocked) }
        // The balance row shows the formatted balance once the dashboard has loaded.
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("1250.5000") > 0 }
    }

    private fun slideToPay() {
        composeRule.onNode(hasContentDescription("Slide")).performTouchInput {
            down(center)
            moveBy(Offset(800f, 0f))
            up()
        }
    }

    private fun typeAddress(text: String) = composeRule.onNodeWithText("Paste your address").performTextInput(text)
    private fun typeAmount(text: String) = composeRule.onNodeWithText("Enter Amount").performTextInput(text)

    @Test
    fun garbageAddressIsFlaggedWhileTyping() {
        open()
        typeAddress("hello")
        composeRule.onNodeWithText(AddressValidator.FORMAT_ERROR).assertIsDisplayed()
    }

    @Test
    fun ownAddressIsRejected() {
        open()
        typeAddress(Sample.OWN)
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount(AddressValidator.OWN_ADDRESS_ERROR) > 0 }
        composeRule.onNodeWithText(AddressValidator.OWN_ADDRESS_ERROR).assertIsDisplayed()
    }

    @Test
    fun blankAddressIsReportedOnPay() {
        open()
        typeAmount("1")
        slideToPay()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("Please enter the recipient address") > 0 }
    }

    @Test
    fun amountAboveBalanceIsReported() {
        open()
        typeAddress(Sample.OTHER)
        typeAmount("5000")
        slideToPay()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount(AmountValidator.INSUFFICIENT_BALANCE) > 0 }
        composeRule.onNodeWithText(AmountValidator.INSUFFICIENT_BALANCE).assertIsDisplayed()
        assertEquals("nothing may be sent", 0, chain.broadcasts.get())
    }

    @Test
    fun validRequestAsksForConfirmationShowingRecipientAmountAndFee() {
        open()
        typeAddress(Sample.OTHER)
        typeAmount("1.5")
        slideToPay()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("Confirm payment") > 0 }
        composeRule.onNodeWithText("Send 1.5 EZPT").assertIsDisplayed()
        composeRule.onNodeWithText("To: ${Sample.OTHER}").assertIsDisplayed()
        composeRule.onNodeWithText("Estimated network fee: 0.000105 BNB").assertIsDisplayed()
        assertEquals("nothing is sent before Confirm", 0, chain.broadcasts.get())
    }

    @Test
    fun cancelClosesTheConfirmationWithoutSending() {
        open()
        typeAddress(Sample.OTHER)
        typeAmount("1.5")
        slideToPay()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("Confirm payment") > 0 }
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("Confirm payment") == 0 }
        assertEquals(0, chain.broadcasts.get())
    }

    @Test
    fun networkRejectionShowsAnErrorDialogWithAnAccessibleIcon() {
        chain.gasError = "execution reverted"
        open()
        typeAddress(Sample.OTHER)
        typeAmount("1.5")
        slideToPay()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("The network rejected this payment") > 0 }
        composeRule.onNodeWithContentDescription("Error").assertIsDisplayed()
        composeRule.onNodeWithText("Done").performClick()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("The network rejected this payment") == 0 }
    }

    @Test
    fun tokenPickerAndSlideControlAreLabelled() {
        open()
        composeRule.onNodeWithContentDescription("Select Token").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Slide").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Paste address").assertIsDisplayed()
    }
}
