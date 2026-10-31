package com.app.ezipaycoin.presentation.shared

import app.cash.turbine.test
import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.domain.repository.HomeRepository
import com.app.ezipaycoin.domain.repository.UserDataRepository
import com.app.ezipaycoin.testutil.FakeDataStore
import com.app.ezipaycoin.testutil.Fixtures
import com.app.ezipaycoin.testutil.MainDispatcherRule
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Dashboard refresh rules: load at unlock, throttled refresh on resume and push, pull-to-refresh
 * always fetches, and a failed background refresh keeps the last good data on screen.
 *
 * The throttles take their time from [now], which the tests move by hand.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WalletSharedViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val home = mockk<HomeRepository>()
    private val data = mockk<UserDataRepository>(relaxed = true)

    private val ezpt = Fixtures.crypto("EZPT", 100.0)
    private val bnb = Fixtures.crypto("BNB", 1.0)
    private val usdt = Fixtures.crypto("USDT", 5.0)

    @Before
    fun setUp() {
        FakeDataStore.install(FakeDataStore(UserPreferences(token = "jwt-from-prefs")))
        coEvery { home.getDashboard() } returns Fixtures.ok(Fixtures.dashboard(ezpt, bnb, usdt))
    }

    @After
    fun tearDown() {
        SessionManager.token = ""
        unmockkAll()
    }

    private var now = 1_000_000L

    private fun vm() = WalletSharedViewModel(home, data) { now }.also { main.scheduler.runCurrent() }

    private fun WalletSharedViewModel.unlock() {
        onEvent(SharedEvent.AppUnlocked)
        main.scheduler.runCurrent()
    }

    private fun WalletSharedViewModel.send(e: SharedEvent) {
        onEvent(e)
        main.scheduler.runCurrent()
    }

    private val WalletSharedViewModel.state get() = uiState.value

    @Test
    fun nothingIsFetchedBeforeUnlock() {
        val vm = vm()
        assertEquals(ResponseState.Idle, vm.state.responseState)
        coVerify(exactly = 0) { home.getDashboard() }
    }

    @Test
    fun unlockLoadsTheDashboardAndSelectsTheFirstCoin() {
        val vm = vm()
        vm.unlock()
        assertTrue(vm.state.isUnlocked)
        assertTrue(vm.state.responseState is ResponseState.Success)
        assertEquals("EZPT", vm.state.selectedCrypto?.symbol)
        assertEquals(3, vm.state.dashboardResponse?.crypto?.size)
        assertEquals("jwt-from-prefs", SessionManager.token)
    }

    @Test
    fun unlockShowsTheLoaderWhileFirstLoading() {
        val gate = CompletableDeferred<BaseResponse<DashboardResponse>>()
        coEvery { home.getDashboard() } coAnswers { gate.await() }
        val vm = vm()
        vm.unlock()
        assertEquals(ResponseState.Loading, vm.state.responseState)
        gate.complete(Fixtures.ok(Fixtures.dashboard(ezpt)))
        main.scheduler.runCurrent()
        assertTrue(vm.state.responseState is ResponseState.Success)
    }

    @Test
    fun firstLoadFailureShowsAnError() {
        coEvery { home.getDashboard() } throws IOException("offline")
        val vm = vm()
        vm.unlock()
        assertEquals(ResponseState.Error("offline"), vm.state.responseState)
        assertNull(vm.state.dashboardResponse)
    }

    @Test
    fun serverRefusalOnFirstLoadShowsItsMessage() {
        coEvery { home.getDashboard() } returns BaseResponse(false, "FEATURE_DISABLED", "", Fixtures.dashboard())
        val vm = vm()
        vm.unlock()
        assertEquals(ResponseState.Error("FEATURE_DISABLED"), vm.state.responseState)
    }

    @Test
    fun selectedCoinSurvivesARefreshEvenWithDifferentCase() {
        val vm = vm()
        vm.unlock()
        vm.send(SharedEvent.CryptoOptionChanged(bnb))
        coEvery { home.getDashboard() } returns Fixtures.ok(
            Fixtures.dashboard(ezpt, Fixtures.crypto("bnb", 2.0), usdt)
        )
        vm.send(SharedEvent.PullRefresh)
        assertEquals(2.0, vm.state.selectedCrypto?.balance ?: 0.0, 0.0)
    }

    @Test
    fun selectionFallsBackToTheFirstCoinWhenItIsNoLongerListed() {
        val vm = vm()
        vm.unlock()
        vm.send(SharedEvent.CryptoOptionChanged(usdt))
        coEvery { home.getDashboard() } returns Fixtures.ok(Fixtures.dashboard(ezpt, bnb))
        vm.send(SharedEvent.PullRefresh)
        assertEquals("EZPT", vm.state.selectedCrypto?.symbol)
    }

    @Test
    fun resumeRefreshIsIgnoredBeforeUnlock() {
        val vm = vm()
        vm.send(SharedEvent.RefreshIfStale)
        coVerify(exactly = 0) { home.getDashboard() }
    }

    @Test
    fun resumeRefreshIsThrottledAndSilent() {
        val vm = vm()
        vm.unlock()
        coVerify(exactly = 1) { home.getDashboard() }

        vm.send(SharedEvent.RefreshIfStale) // first resume after unlock: allowed
        coVerify(exactly = 2) { home.getDashboard() }
        assertTrue("silent refresh keeps data", vm.state.responseState is ResponseState.Success)

        vm.send(SharedEvent.RefreshIfStale) // immediately again: throttled
        vm.send(SharedEvent.RefreshIfStale)
        coVerify(exactly = 2) { home.getDashboard() }
    }

    @Test
    fun resumeRefreshIsAllowedAgainOnceTheWindowHasPassed() {
        val vm = vm()
        vm.unlock()
        vm.send(SharedEvent.RefreshIfStale)
        coVerify(exactly = 2) { home.getDashboard() }

        now += 9_999
        vm.send(SharedEvent.RefreshIfStale)
        coVerify(exactly = 2) { home.getDashboard() }

        now += 1
        vm.send(SharedEvent.RefreshIfStale)
        coVerify(exactly = 3) { home.getDashboard() }
    }

    @Test
    fun pushRefreshIsAllowedAgainAfterTwoSeconds() {
        val vm = vm()
        vm.unlock()
        SessionManager.notifyTransferActivity()
        main.scheduler.runCurrent()
        coVerify(exactly = 2) { home.getDashboard() }

        now += 1_999
        SessionManager.notifyTransferActivity()
        main.scheduler.runCurrent()
        coVerify(exactly = 2) { home.getDashboard() }

        now += 1
        SessionManager.notifyTransferActivity()
        main.scheduler.runCurrent()
        coVerify(exactly = 3) { home.getDashboard() }
    }

    @Test
    fun silentRefreshDoesNotShowTheFullScreenLoader() {
        val vm = vm()
        vm.unlock()
        val gate = CompletableDeferred<BaseResponse<DashboardResponse>>()
        coEvery { home.getDashboard() } coAnswers { gate.await() }
        vm.send(SharedEvent.RefreshIfStale)
        assertTrue(vm.state.responseState is ResponseState.Success)
        gate.complete(Fixtures.ok(Fixtures.dashboard(ezpt)))
        main.scheduler.runCurrent()
    }

    @Test
    fun pullRefreshAlwaysFetchesAndTogglesTheIndicator() {
        val vm = vm()
        vm.unlock()
        val gate = CompletableDeferred<BaseResponse<DashboardResponse>>()
        coEvery { home.getDashboard() } coAnswers { gate.await() }

        vm.send(SharedEvent.PullRefresh)
        assertTrue(vm.state.isRefreshing)
        assertTrue(vm.state.responseState is ResponseState.Success)
        gate.complete(Fixtures.ok(Fixtures.dashboard(ezpt, bnb)))
        main.scheduler.runCurrent()
        assertFalse(vm.state.isRefreshing)
        assertEquals(2, vm.state.dashboardResponse?.crypto?.size)
    }

    @Test
    fun pullRefreshBypassesTheThrottle() {
        val vm = vm()
        vm.unlock()
        vm.send(SharedEvent.RefreshIfStale)
        vm.send(SharedEvent.PullRefresh)
        vm.send(SharedEvent.PullRefresh)
        coVerify(exactly = 4) { home.getDashboard() }
    }

    @Test
    fun pullRefreshIsIgnoredBeforeUnlock() {
        val vm = vm()
        vm.send(SharedEvent.PullRefresh)
        coVerify(exactly = 0) { home.getDashboard() }
    }

    @Test
    fun failedPullRefreshKeepsLastGoodData() {
        val vm = vm()
        vm.unlock()
        coEvery { home.getDashboard() } throws IOException("offline")
        vm.send(SharedEvent.PullRefresh)
        assertTrue(vm.state.responseState is ResponseState.Success)
        assertEquals(3, vm.state.dashboardResponse?.crypto?.size)
        assertFalse(vm.state.isRefreshing)
    }

    @Test
    fun serverRefusalDuringRefreshKeepsLastGoodData() {
        val vm = vm()
        vm.unlock()
        coEvery { home.getDashboard() } returns BaseResponse(false, "boom", "", Fixtures.dashboard())
        vm.send(SharedEvent.PullRefresh)
        assertTrue(vm.state.responseState is ResponseState.Success)
        assertEquals(3, vm.state.dashboardResponse?.crypto?.size)
        assertFalse(vm.state.isRefreshing)
    }

    @Test
    fun pushWhileUnlockedRefreshesOnce() {
        val vm = vm()
        vm.unlock()
        SessionManager.notifyTransferActivity()
        main.scheduler.runCurrent()
        coVerify(exactly = 2) { home.getDashboard() }

        SessionManager.notifyTransferActivity() // inside the push window
        main.scheduler.runCurrent()
        coVerify(exactly = 2) { home.getDashboard() }
    }

    @Test
    fun pushBeforeUnlockIsIgnored() {
        vm()
        SessionManager.notifyTransferActivity()
        main.scheduler.runCurrent()
        coVerify(exactly = 0) { home.getDashboard() }
    }

    @Test
    fun pushRefreshRestartsTheResumeWindow() {
        val vm = vm()
        vm.unlock()
        SessionManager.notifyTransferActivity()
        main.scheduler.runCurrent()
        coVerify(exactly = 2) { home.getDashboard() }
        vm.send(SharedEvent.RefreshIfStale) // a push just refreshed, so resume is throttled
        coVerify(exactly = 2) { home.getDashboard() }
    }

    @Test
    fun fetchBalanceEventReloadsWithLoader() {
        val vm = vm()
        vm.unlock()
        vm.send(SharedEvent.FetchBalance)
        coVerify(exactly = 2) { home.getDashboard() }
    }

    @Test
    fun transactionSelectionIsStored() {
        val vm = vm()
        val tx = Fixtures.txItem("abc")
        vm.send(SharedEvent.TransactionSelected(tx))
        assertEquals("abc", vm.state.selectedTransaction?.id)
    }

    @Test
    fun openUrlEventIsRelayedToCollectors() = kotlinx.coroutines.test.runTest(main.dispatcher) {
        val vm = vm()
        vm.eventFlow.test {
            vm.onEvent(SharedEvent.OpenUrl("https://example.org"))
            runCurrent()
            assertEquals(SharedEvent.OpenUrl("https://example.org"), awaitItem())
        }
    }

    @Test
    fun socialLinksMapToTheirPages() = kotlinx.coroutines.test.runTest(main.dispatcher) {
        val vm = vm()
        vm.eventFlow.test {
            vm.onOpenLinkClicked("X")
            runCurrent()
            assertEquals(SharedEvent.OpenUrl("https://x.com/EzipayCoin"), awaitItem())
            vm.onOpenLinkClicked("Privacy Policy")
            runCurrent()
            assertTrue((awaitItem() as SharedEvent.OpenUrl).url.contains("Privacy-Policy"))
            vm.onOpenLinkClicked("something else")
            runCurrent()
            assertEquals(SharedEvent.OpenUrl("https://www.ezipaycoin.com/"), awaitItem())
        }
    }
}
