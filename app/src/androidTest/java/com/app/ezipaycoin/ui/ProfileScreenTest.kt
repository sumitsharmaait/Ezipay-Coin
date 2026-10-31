package com.app.ezipaycoin.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.ezipaycoin.data.remote.dto.response.MeProfileResponse
import com.app.ezipaycoin.presentation.profile.MyProfile
import com.app.ezipaycoin.presentation.profile.MyProfileViewModel
import com.app.ezipaycoin.support.FakeAuthRepository
import com.app.ezipaycoin.support.FakeUserProfileRepository
import com.app.ezipaycoin.support.Sample
import com.app.ezipaycoin.support.ScreenTest
import com.app.ezipaycoin.support.onAllNodesWithTextCount
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest : ScreenTest() {
    private fun open(profiles: FakeUserProfileRepository) {
        val vm = onMain { MyProfileViewModel(FakeAuthRepository(), profiles) }
        show { MyProfile(rememberNavController(), vm) }
    }

    @Test
    fun showsTheNameFromTheServer() {
        seed { it.copy(userName = "Cached Name", walletAddress = Sample.OWN) }
        open(FakeUserProfileRepository(MeProfileResponse(Sample.OWN, "Server Name", "me@example.org", null, "approved")))
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("Server Name") > 0 }
        composeRule.onNodeWithText("Server Name").assertIsDisplayed()
    }

    @Test
    fun showsTheCachedNameWhenOffline() {
        seed { it.copy(userName = "Cached Name", walletAddress = Sample.OWN) }
        open(FakeUserProfileRepository(profile = null))
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("Cached Name") > 0 }
        composeRule.onNodeWithText("Cached Name").assertIsDisplayed()
    }

    @Test
    fun profileImageAndEditControlsAreLabelled() {
        seed { it.copy(userName = "Cached Name") }
        open(FakeUserProfileRepository(profile = null))
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithTextCount("Cached Name") > 0 }
        composeRule.onNodeWithContentDescription("Profile Picture").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Edit name").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Edit Profile Picture").assertIsDisplayed()
    }
}
