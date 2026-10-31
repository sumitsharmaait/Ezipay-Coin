package com.app.ezipaycoin.presentation

import com.app.ezipaycoin.data.remote.dto.response.LearnItemResponse
import com.app.ezipaycoin.domain.repository.LearnRepository
import com.app.ezipaycoin.presentation.dashboard.earn.Load
import com.app.ezipaycoin.presentation.dashboard.learn.LearnViewModel
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LearnViewModelTest {

    private class FakeLearn(var result: ResponseState<List<LearnItemResponse>>) : LearnRepository {
        override suspend fun items() = result
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun item(id: Int, order: Int?, status: String? = "active", title: String? = "T$id") =
        LearnItemResponse(id = id, title = title, sortOrder = order, status = status)

    @Test
    fun `shows active titled items in the server order`() {
        val vm = LearnViewModel(
            FakeLearn(
                ResponseState.Success(
                    listOf(item(1, 3), item(2, 1), item(3, 2, status = "inactive"), item(4, 0, title = " "), item(5, null, status = null))
                )
            )
        )
        val ready = vm.uiState.value.lessons as Load.Ready
        assertEquals(listOf(2, 1, 5), ready.data.map { it.id })
    }

    @Test
    fun `an empty list is an empty state not an error`() {
        val vm = LearnViewModel(FakeLearn(ResponseState.Success(emptyList())))
        assertEquals(emptyList<LearnItemResponse>(), (vm.uiState.value.lessons as Load.Ready).data)
    }

    @Test
    fun `failure can be retried`() {
        val repo = FakeLearn(ResponseState.Error("Can't reach the network. Check your connection and try again."))
        val vm = LearnViewModel(repo)
        assertTrue(vm.uiState.value.lessons is Load.Failed)
        repo.result = ResponseState.Success(listOf(item(1, 1)))
        vm.load()
        assertTrue(vm.uiState.value.lessons is Load.Ready)
    }
}
