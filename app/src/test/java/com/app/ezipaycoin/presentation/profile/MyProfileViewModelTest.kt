package com.app.ezipaycoin.presentation.profile

import android.net.Uri
import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.data.remote.dto.request.UpdateProfileRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.MeProfileResponse
import com.app.ezipaycoin.domain.repository.AuthRepository
import com.app.ezipaycoin.domain.repository.UserProfileRepository
import com.app.ezipaycoin.testutil.FakeDataStore
import com.app.ezipaycoin.testutil.Fixtures
import com.app.ezipaycoin.testutil.MainDispatcherRule
import com.app.ezipaycoin.utils.ResponseState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException

/** Profile screen state: cached values first, Users/me on top, cache kept when the server is unreachable. */
@OptIn(ExperimentalCoroutinesApi::class)
class MyProfileViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val auth = mockk<AuthRepository>()
    private val profiles = mockk<UserProfileRepository>()

    @After
    fun tearDown() = unmockkAll()

    private fun me(name: String? = "Server Name", email: String? = "server@example.org", pic: String? = "https://img/s.png") =
        BaseResponse(true, "OK", "", MeProfileResponse(Fixtures.OWN_ADDRESS, name, email, pic, "Approved"))

    private fun store(prefs: UserPreferences = UserPreferences(
        walletAddress = Fixtures.OWN_ADDRESS, userName = "Cached Name", userEmail = "cached@example.org", userProfile = "https://img/c.png"
    )) = FakeDataStore(prefs).also { FakeDataStore.install(it) }

    private fun vm(repo: UserProfileRepository? = profiles) =
        MyProfileViewModel(auth, repo).also { main.scheduler.runCurrent() }

    @Test
    fun showsCachedValuesWhenTheServerIsOffline() {
        store()
        coEvery { profiles.getMe() } throws IOException("offline")
        val s = vm().uiState.value
        assertEquals("Cached Name", s.name)
        assertEquals("cached@example.org", s.email)
        assertEquals(Fixtures.OWN_ADDRESS, s.walletAddress)
    }

    @Test
    fun serverProfileReplacesAndIsCached() {
        val ds = store()
        coEvery { profiles.getMe() } returns me()
        val s = vm().uiState.value
        assertEquals("Server Name", s.name)
        assertEquals("server@example.org", s.email)
        assertEquals("https://img/s.png", s.profile)
        assertEquals("Server Name", ds.current.userName)
        assertEquals("server@example.org", ds.current.userEmail)
        assertEquals("https://img/s.png", ds.current.userProfile)
    }

    @Test
    fun blankServerFieldsKeepTheCachedOnes() {
        val ds = store()
        coEvery { profiles.getMe() } returns me(name = "  ", email = null, pic = "")
        val s = vm().uiState.value
        assertEquals("Cached Name", s.name)
        assertEquals("cached@example.org", s.email)
        assertEquals("Cached Name", ds.current.userName)
    }

    @Test
    fun refusedResponseLeavesTheCacheAlone() {
        val ds = store()
        coEvery { profiles.getMe() } returns BaseResponse(false, "UN001", "", MeProfileResponse(null, "X", "x", null, null))
        val s = vm().uiState.value
        assertEquals("Cached Name", s.name)
        assertEquals("Cached Name", ds.current.userName)
    }

    @Test
    fun emptyCacheShowsPlaceholdersUntilTheServerAnswers() {
        store(UserPreferences())
        coEvery { profiles.getMe() } throws IOException("offline")
        val s = vm().uiState.value
        assertEquals("Not Available", s.name)
        assertEquals("Not Available", s.email)
    }

    @Test
    fun withoutAProfileRepositoryOnlyTheCacheIsUsed() {
        store()
        val s = vm(repo = null).uiState.value
        assertEquals("Cached Name", s.name)
    }

    @Test
    fun renameCallsTheApiWithNameAndEmailAndUpdatesTheCache() {
        val ds = store()
        coEvery { profiles.getMe() } returns me(name = "Cached Name", email = "cached@example.org", pic = null)
        coEvery { auth.updateProfile(any()) } returns BaseResponse(true, "OK", "", "done")
        val vm = vm()
        vm.onEvent(MyProfileEvent.UpdateName("New Name"))
        main.scheduler.runCurrent()
        coVerify { auth.updateProfile(UpdateProfileRequest("New Name", "cached@example.org")) }
        assertEquals("New Name", ds.current.userName)
        assertTrue(vm.uiState.value.responseState is ResponseState.Success)
    }

    @Test
    fun renameFailureIsShownAndCacheIsUntouched() {
        val ds = store()
        coEvery { profiles.getMe() } throws IOException("offline")
        coEvery { auth.updateProfile(any()) } throws IOException("no route")
        val vm = vm()
        vm.onEvent(MyProfileEvent.UpdateName("New Name"))
        main.scheduler.runCurrent()
        assertEquals(ResponseState.Error("no route"), vm.uiState.value.responseState)
        assertEquals("Cached Name", ds.current.userName)
    }

    @Test
    fun logoutSucceeds() {
        store()
        coEvery { profiles.getMe() } throws IOException("offline")
        val res = BaseResponse(true, "OK", "", "bye")
        coEvery { auth.logout() } returns res
        val vm = vm()
        vm.onEvent(MyProfileEvent.LogOut)
        main.scheduler.runCurrent()
        assertEquals(ResponseState.Success(res), vm.uiState.value.logoutResponseState)
    }

    @Test
    fun logoutFailureIsShown() {
        store()
        coEvery { profiles.getMe() } throws IOException("offline")
        coEvery { auth.logout() } throws IOException("down")
        val vm = vm()
        vm.onEvent(MyProfileEvent.LogOut)
        main.scheduler.runCurrent()
        assertEquals(ResponseState.Error("down"), vm.uiState.value.logoutResponseState)
    }

    @Test
    fun uploadedPictureUrlIsCached() {
        val ds = store()
        coEvery { profiles.getMe() } throws IOException("offline")
        coEvery { auth.updateImage(any()) } returns BaseResponse(true, "OK", "", "https://img/new.png")
        val file = File.createTempFile("avatar", ".png").apply { writeBytes(byteArrayOf(1, 2, 3)); deleteOnExit() }
        val vm = vm()
        vm.onEvent(MyProfileEvent.ImageSelected(mockk<Uri>(relaxed = true), file))
        main.scheduler.runCurrent()
        assertEquals("https://img/new.png", ds.current.userProfile)
        assertTrue(vm.uiState.value.uploadImageResponseState is ResponseState.Success)
    }
}
