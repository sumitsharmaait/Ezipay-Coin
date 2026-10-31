package com.app.ezipaycoin.presentation.dashboard.earn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.response.DailyTask
import com.app.ezipaycoin.domain.repository.EarnRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

class EarnViewModel(
    private val repository: EarnRepository,
    /** The backend decides "today" in UTC. */
    private val today: () -> LocalDate = { LocalDate.now(ZoneOffset.UTC) },
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val newKey: () -> String = { UUID.randomUUID().toString() },
) : ViewModel() {

    private val _state = MutableStateFlow(EarnState())
    val state: StateFlow<EarnState> = _state.asStateFlow()

    private val openedAt = mutableMapOf<Int, Long>()
    private var pendingCheckInKey: String? = null
    private val pendingTaskKeys = mutableMapOf<Int, String>()
    private var loadedPools = false

    init {
        refresh()
    }

    fun refresh() {
        loadTotals()
        loadCheckIn()
        loadTasks()
    }

    /** Pools are only requested when the staking tab is shown. */
    fun loadPoolsOnce() {
        if (loadedPools) return
        loadedPools = true
        viewModelScope.launch {
            val result = repository.stakingPools()
            _state.update { it.copy(pools = result.toLoad()) }
        }
    }

    fun retryPools() {
        loadedPools = false
        _state.update { it.copy(pools = Load.Loading) }
        loadPoolsOnce()
    }

    private fun loadTotals() {
        _state.update { it.copy(totals = Load.Loading) }
        viewModelScope.launch {
            val claimable = async { repository.summary(CLAIMABLE) }
            val locked = async { repository.summary(LOCKED) }
            val c = claimable.await()
            val l = locked.await()
            val next: Load<RewardTotals> = when {
                c is ResponseState.Error -> Load.Failed(describe(c.message))
                l is ResponseState.Error -> Load.Failed(describe(l.message))
                c is ResponseState.Success && l is ResponseState.Success -> Load.Ready(
                    RewardTotals(
                        symbol = c.data.tokenSymbol?.takeIf { it.isNotBlank() }
                            ?: l.data.tokenSymbol?.takeIf { it.isNotBlank() } ?: DEFAULT_SYMBOL,
                        claimable = c.data.totalAmount ?: 0.0,
                        locked = l.data.totalAmount ?: 0.0,
                    )
                )
                else -> Load.Failed(GENERIC)
            }
            _state.update { it.copy(totals = next) }
        }
    }

    private fun loadCheckIn() {
        _state.update { it.copy(checkIn = Load.Loading) }
        viewModelScope.launch {
            val month = today().format(MONTH)
            val result = repository.checkInStatus(month)
            _state.update { it.copy(checkIn = result.toLoad()) }
        }
    }

    private fun loadTasks() {
        _state.update { it.copy(tasks = Load.Loading) }
        viewModelScope.launch {
            val result = repository.tasks(today().format(DAY))
            _state.update {
                it.copy(
                    tasks = when (result) {
                        is ResponseState.Success -> Load.Ready(result.data.tasks.orEmpty())
                        is ResponseState.Error -> Load.Failed(describe(result.message))
                        else -> Load.Loading
                    }
                )
            }
        }
    }

    fun checkIn() {
        val current = _state.value
        if (current.checkInBusy) return
        val status = (current.checkIn as? Load.Ready)?.data
        if (status != null && !status.canCheckIn) {
            _state.update { it.copy(message = describe("ALREADY_CHECKED_IN")) }
            return
        }
        val key = pendingCheckInKey ?: newKey().also { pendingCheckInKey = it }
        _state.update { it.copy(checkInBusy = true, message = null) }
        viewModelScope.launch {
            when (val result = repository.checkIn(today().format(DAY), key)) {
                is ResponseState.Success -> {
                    pendingCheckInKey = null
                    val reward = result.data.reward
                    val text = "Checked in. Day ${result.data.newStreak}" +
                        (reward?.amount?.let { " - you earned $it ${reward.tokenSymbol.orEmpty()}".trimEnd() } ?: "")
                    _state.update { it.copy(checkInBusy = false, message = text) }
                    loadCheckIn()
                    loadTotals()
                }
                is ResponseState.Error -> {
                    // A refusal by rule is final; a network failure keeps the key for the retry.
                    if (isRuleCode(result.message)) pendingCheckInKey = null
                    _state.update { it.copy(checkInBusy = false, message = describe(result.message)) }
                    if (result.message == "ALREADY_CHECKED_IN") loadCheckIn()
                }
                else -> _state.update { it.copy(checkInBusy = false) }
            }
        }
    }

    /** The user opened the task's content; the clock for "watched" starts now. */
    fun onTaskOpened(taskId: Int) {
        openedAt[taskId] = nowMillis()
    }

    fun completeTask(task: DailyTask) {
        if (_state.value.completingTaskId != null) return
        val required = task.requiredSeconds
        var watched: Int? = null
        if (required != null && required > 0) {
            val start = openedAt[task.taskId]
            val elapsed = if (start == null) 0 else ((nowMillis() - start) / 1000).toInt()
            if (elapsed < required) {
                val left = required - elapsed
                val text = if (start == null) "Open the task first, then come back to finish it."
                else "Keep going - $left more seconds."
                _state.update { it.copy(message = text) }
                return
            }
            watched = elapsed
        }
        val key = pendingTaskKeys.getOrPut(task.taskId) { newKey() }
        _state.update { it.copy(completingTaskId = task.taskId, message = null) }
        viewModelScope.launch {
            when (val result = repository.completeTask(task.taskId, today().format(DAY), watched, key)) {
                is ResponseState.Success -> {
                    pendingTaskKeys.remove(task.taskId)
                    val reward = result.data.reward
                    val text = "Task complete" +
                        (reward?.amount?.let { " - you earned $it ${reward.tokenSymbol.orEmpty()}".trimEnd() } ?: "")
                    _state.update { it.copy(completingTaskId = null, message = text) }
                    loadTasks()
                    loadTotals()
                }
                is ResponseState.Error -> {
                    if (isRuleCode(result.message)) pendingTaskKeys.remove(task.taskId)
                    _state.update { it.copy(completingTaskId = null, message = describe(result.message)) }
                    if (result.message == "TASK_ALREADY_COMPLETED") loadTasks()
                }
                else -> _state.update { it.copy(completingTaskId = null) }
            }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun <T> ResponseState<T>.toLoad(): Load<T> = when (this) {
        is ResponseState.Success -> Load.Ready(data)
        is ResponseState.Error -> Load.Failed(describe(message))
        else -> Load.Loading
    }

    companion object {
        const val CLAIMABLE = "CLAIMABLE"
        const val LOCKED = "LOCKED"
        const val DEFAULT_SYMBOL = "EZP"
        private const val GENERIC = "Something went wrong. Please try again."
        private val DAY: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

        private val CODE = Regex("^[A-Z][A-Z0-9_]+(\\|.*)?$")

        /** Server guard codes are upper snake case; anything else is a transport message. */
        fun isRuleCode(message: String): Boolean = CODE.matches(message)

        fun describe(message: String): String = when (message.substringBefore('|')) {
            "ALREADY_CHECKED_IN" -> "You have already checked in today."
            "INVALID_DATE" -> "Your device date doesn't match the server's. Check your clock."
            "TASK_NOT_FOUND" -> "That task no longer exists."
            "TASK_NOT_ACTIVE" -> "That task isn't available today."
            "TASK_ALREADY_COMPLETED" -> "You've already completed that task today."
            "INSUFFICIENT_WATCH_TIME" -> "Spend a little longer on the task, then try again."
            "FEATURE_DISABLED" -> "This isn't available yet."
            "NOT_FOUND" -> "Not available right now."
            else -> if (isRuleCode(message)) GENERIC else message
        }
    }
}
