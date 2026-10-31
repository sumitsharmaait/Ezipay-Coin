package com.app.ezipaycoin.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.ezipaycoin.support.ScreenTest
import com.app.ezipaycoin.ui.composables.Dialogue
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class DialogueAccessibilityTest : ScreenTest() {
    @Test
    fun errorDialogAnnouncesAnErrorAndDismisses() {
        val dismissed = AtomicBoolean(false)
        show { Dialogue(isError = true, msg = "Something broke") { dismissed.set(true) } }
        composeRule.onNodeWithContentDescription("Error").assertIsDisplayed()
        composeRule.onNodeWithText("Something broke").assertIsDisplayed()
        composeRule.onNodeWithText("Done").performClick()
        assertTrue(dismissed.get())
    }

    @Test
    fun successDialogAnnouncesSuccess() {
        show { Dialogue(isError = false, msg = "All good") {} }
        composeRule.onNodeWithContentDescription("Success").assertIsDisplayed()
        composeRule.onNodeWithText("All good").assertIsDisplayed()
    }
}
