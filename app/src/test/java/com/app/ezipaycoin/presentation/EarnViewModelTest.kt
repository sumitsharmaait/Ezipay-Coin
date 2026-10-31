package com.app.ezipaycoin.presentation

import com.app.ezipaycoin.data.remote.dto.response.CheckInResponse
import com.app.ezipaycoin.data.remote.dto.response.CheckInReward
import com.app.ezipaycoin.data.remote.dto.response.CheckInStatusResponse
import com.app.ezipaycoin.data.remote.dto.response.CompleteTaskResponse
import com.app.ezipaycoin.data.remote.dto.response.CompleteTaskReward
import com.app.ezipaycoin.data.remote.dto.response.DailyTask
import com.app.ezipaycoin.data.remote.dto.response.DailyTasksResponse
import com.app.ezipaycoin.data.remote.dto.response.RewardSummaryResponse
import com.app.ezipaycoin.data.remote.dto.response.StakingPoolResponse
import com.app.ezipaycoin.domain.repository.EarnRepository
import com.app.ezipaycoin.presentation.dashboard.earn.EarnViewModel
import com.app.ezipaycoin.presentation.dashboard.earn.Load
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class EarnViewModelTest {

    private class FakeRepo : EarnRepository {
        var claimable: ResponseState<RewardSummaryResponse> =
            ResponseState.Success(RewardSummaryResponse("EZP", "CLAIMABLE", 1.5, 1))
        var locked: ResponseState<RewardSummaryResponse> =
            ResponseState.Success(RewardSummaryResponse("EZP", "LOCKED", 2.0, 1))
        var status: ResponseState<CheckInStatusResponse> =
            ResponseState.Success(CheckInStatusResponse("2026-10", "2026-10-31", emptyList(), true, 0))
        var tasks: ResponseState<DailyTasksResponse> = ResponseState.Success(DailyTasksResponse("2026-10-31", emptyList()))
        var checkInResult: ResponseState<CheckInResponse> =
            ResponseState.Success(CheckInResponse("2026-10-31", 1, CheckInReward("2.0000", "EZP")))
        var completeResult: ResponseState<CompleteTaskResponse> =
            ResponseState.Success(CompleteTaskResponse(1, "2026-10-31", "COMPLETED", CompleteTaskReward("1.0000", "EZP")))
        var pools: ResponseState<List<StakingPoolResponse>> = ResponseState.Success(emptyList())

        val checkInKeys = mutableListOf<String>()
        val completeCalls = mutableListOf<Triple<Int, Int?, String>>()
        var poolCalls = 0

        override suspend fun summary(status: String) = if (status == "CLAIMABLE") claimable else locked
        override suspend fun checkInStatus(month: String) = status
        override suspend fun checkIn(date: String, idempotencyKey: String): ResponseState<CheckInResponse> {
            checkInKeys += idempotencyKey
            return checkInResult
        }
        override suspend fun tasks(date: String) = tasks
        override suspend fun completeTask(taskId: Int, date: String, watchedSeconds: Int?, idempotencyKey: String):
            ResponseState<CompleteTaskResponse> {
            completeCalls += Triple(taskId, watchedSeconds, idempotencyKey)
            return completeResult
        }
        override suspend fun stakingPools(): ResponseState<List<StakingPoolResponse>> {
            poolCalls++
            return pools
        }
    }

    private lateinit var repo: FakeRepo
    private var clock = 0L
    private var keyCounter = 0

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repo = FakeRepo()
        clock = 0L
        keyCounter = 0
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun vm() = EarnViewModel(
        repo,
        today = { LocalDate.of(2026, 10, 31) },
        nowMillis = { clock },
        newKey = { "key-${++keyCounter}" }
    )

    private fun task(id: Int = 1, required: Int? = 30, status: String = "PENDING") =
        DailyTask(taskId = id, title = "t", requiredSeconds = required, status = status)

    @Test
    fun `loads totals check-in and tasks on start`() {
        repo.tasks = ResponseState.Success(DailyTasksResponse("2026-10-31", listOf(task())))
        val s = vm().state.value
        val totals = (s.totals as Load.Ready).data
        assertEquals(1.5, totals.claimable, 0.0)
        assertEquals(2.0, totals.locked, 0.0)
        assertEquals(3.5, totals.total, 0.0)
        assertEquals("EZP", totals.symbol)
        assertTrue((s.checkIn as Load.Ready).data.canCheckIn)
        assertEquals(1, (s.tasks as Load.Ready).data.size)
    }

    @Test
    fun `an empty summary shows zero not fake numbers`() {
        repo.claimable = ResponseState.Success(RewardSummaryResponse())
        repo.locked = ResponseState.Success(RewardSummaryResponse())
        val totals = (vm().state.value.totals as Load.Ready).data
        assertEquals(0.0, totals.total, 0.0)
        assertEquals("EZP", totals.symbol)
    }

    @Test
    fun `a failed summary is an error state and others still load`() {
        repo.locked = ResponseState.Error("Can't reach the network. Check your connection and try again.")
        val s = vm().state.value
        assertTrue(s.totals is Load.Failed)
        assertTrue(s.checkIn is Load.Ready)
    }

    @Test
    fun `tasks failure is reported with a readable message`() {
        repo.tasks = ResponseState.Error("FEATURE_DISABLED")
        val failed = vm().state.value.tasks as Load.Failed
        assertEquals("This isn't available yet.", failed.message)
    }

    @Test
    fun `check-in success shows the reward and reloads`() {
        val vm = vm()
        repo.status = ResponseState.Success(CheckInStatusResponse("2026-10", "2026-10-31", listOf("2026-10-31"), false, 1))
        vm.checkIn()
        val s = vm.state.value
        assertEquals("Checked in. Day 1 - you earned 2.0000 EZP", s.message)
        assertEquals(false, s.checkInBusy)
        assertEquals(false, (s.checkIn as Load.Ready).data.canCheckIn)
    }

    @Test
    fun `check-in is not sent when the server says it is done for today`() {
        repo.status = ResponseState.Success(CheckInStatusResponse("2026-10", "2026-10-31", listOf("2026-10-31"), false, 1))
        val vm = vm()
        vm.checkIn()
        assertTrue(repo.checkInKeys.isEmpty())
        assertEquals("You have already checked in today.", vm.state.value.message)
    }

    @Test
    fun `a network failure keeps the idempotency key for the retry`() {
        val vm = vm()
        repo.checkInResult = ResponseState.Error("Can't reach the network. Check your connection and try again.")
        vm.checkIn()
        repo.checkInResult = ResponseState.Success(CheckInResponse("2026-10-31", 1, null))
        vm.checkIn()
        assertEquals(listOf("key-1", "key-1"), repo.checkInKeys)
    }

    @Test
    fun `a rule refusal drops the key and maps the code`() {
        val vm = vm()
        repo.checkInResult = ResponseState.Error("ALREADY_CHECKED_IN")
        vm.checkIn()
        assertEquals("You have already checked in today.", vm.state.value.message)
        vm.checkIn()
        assertEquals(listOf("key-1", "key-2"), repo.checkInKeys)
    }

    @Test
    fun `a timed task cannot be completed before it was opened`() {
        val vm = vm()
        vm.completeTask(task(required = 30))
        assertTrue(repo.completeCalls.isEmpty())
        assertEquals("Open the task first, then come back to finish it.", vm.state.value.message)
    }

    @Test
    fun `a timed task needs the required time and sends the watched seconds`() {
        val vm = vm()
        vm.onTaskOpened(1)
        clock = 10_000
        vm.completeTask(task(required = 30))
        assertTrue(repo.completeCalls.isEmpty())
        assertEquals("Keep going - 20 more seconds.", vm.state.value.message)

        clock = 31_000
        vm.completeTask(task(required = 30))
        assertEquals(listOf(Triple(1, 31, "key-1")), repo.completeCalls)
        assertEquals("Task complete - you earned 1.0000 EZP", vm.state.value.message)
        assertNull(vm.state.value.completingTaskId)
    }

    @Test
    fun `a task with no time requirement completes at once`() {
        val vm = vm()
        vm.completeTask(task(required = null))
        assertEquals(listOf(Triple(1, null, "key-1")), repo.completeCalls)
    }

    @Test
    fun `an already completed task reloads the list`() {
        val vm = vm()
        repo.completeResult = ResponseState.Error("TASK_ALREADY_COMPLETED")
        repo.tasks = ResponseState.Success(DailyTasksResponse("2026-10-31", listOf(task(status = "COMPLETED"))))
        vm.completeTask(task(required = null))
        assertEquals("You've already completed that task today.", vm.state.value.message)
        assertTrue(((vm.state.value.tasks as Load.Ready).data[0]).isCompleted)
    }

    @Test
    fun `pools load once on demand`() {
        val vm = vm()
        assertEquals(0, repo.poolCalls)
        vm.loadPoolsOnce()
        vm.loadPoolsOnce()
        assertEquals(1, repo.poolCalls)
        assertTrue(vm.state.value.pools is Load.Ready)
    }

    @Test
    fun `describe tells server codes from transport messages`() {
        assertEquals("Not available right now.", EarnViewModel.describe("NOT_FOUND"))
        assertEquals(
            "Some tasks are unavailable right now. Please try again later.",
            EarnViewModel.describe("EARN_TASK_TYPE_UNKNOWN")
        )
        assertEquals("Something went wrong. Please try again.", EarnViewModel.describe("SOME_UNKNOWN_CODE"))
        assertEquals("Request failed (500).", EarnViewModel.describe("Request failed (500)."))
        assertEquals("You have already checked in today.", EarnViewModel.describe("ALREADY_CHECKED_IN|x"))
    }
}
