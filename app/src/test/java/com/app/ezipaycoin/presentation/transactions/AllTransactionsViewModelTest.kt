package com.app.ezipaycoin.presentation.transactions

import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.domain.repository.TransactionsRepository
import com.app.ezipaycoin.testutil.Fixtures
import com.app.ezipaycoin.testutil.MainDispatcherRule
import com.app.ezipaycoin.utils.ResponseState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Transaction history list. Two tests pin behaviour that looks wrong (page never advances, loader
 * sticks after an error); they are named *KnownIssue* and recorded in the findings, so a fix shows
 * up as a deliberate test change.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AllTransactionsViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val repo = mockk<TransactionsRepository>()
    private val requests = mutableListOf<TransactionsRequest>()

    private fun page(total: Int, vararg ids: String) = BaseResponse(
        true, "OK", "",
        TransactionsResponse(ids.map { Fixtures.txItem(it) }, 1, 10, total)
    )

    private fun vm(): AllTransactionsViewModel {
        coEvery { repo.getTransactions(any()) } answers {
            val r = firstArg<TransactionsRequest>(); requests += r; page(2, "a", "b")
        }
        return AllTransactionsViewModel(repo).also { main.scheduler.runCurrent() }
    }

    @Test
    fun firstPageLoadsOnCreation() {
        val vm = vm()
        val s = vm.uiState.value
        assertEquals(listOf("a", "b"), s.transactionsList.map { it.id })
        assertFalse(s.isLoading)
        assertTrue(s.responseState is ResponseState.Success)
        assertEquals(TransactionsRequest(chain = "BSC", type = "all", page = 1, size = 10), requests.single())
    }

    @Test
    fun everythingLoadedStopsPagination() {
        val vm = vm()
        assertFalse(vm.uiState.value.canLoadMore) // 2 items of total 2
        vm.loadMoreTransactions()
        main.scheduler.runCurrent()
        assertEquals(1, requests.size)
    }

    @Test
    fun changingTheFilterResetsTheListAndRequestsThatType() {
        val vm = vm()
        vm.onEvent(TransactionsEvent.FilterChanged("Received"))
        main.scheduler.runCurrent()
        assertEquals("received", requests.last().type)
        assertEquals(2, vm.uiState.value.transactionsList.size)
        assertEquals("Received", vm.uiState.value.selectedFilter)
    }

    @Test
    fun emptyResultShowsNoTransactionsMessage() {
        coEvery { repo.getTransactions(any()) } returns BaseResponse(
            true, "OK", "", TransactionsResponse(emptyList(), 1, 10, 0)
        )
        val vm = AllTransactionsViewModel(repo)
        main.scheduler.runCurrent()
        assertEquals(ResponseState.Error("No Transactions Found!!"), vm.uiState.value.responseState)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun pageNumberKnownIssueNeverAdvancesBetweenLoads() {
        coEvery { repo.getTransactions(any()) } answers {
            requests += firstArg<TransactionsRequest>()
            page(5, "x${requests.size}", "y${requests.size}")
        }
        val vm = AllTransactionsViewModel(repo)
        main.scheduler.runCurrent()
        vm.loadMoreTransactions()
        main.scheduler.runCurrent()
        vm.loadMoreTransactions()
        main.scheduler.runCurrent()
        // Expected after a fix: pages 1, 2, 3. Today the same page is requested each time.
        assertEquals(listOf(1, 1, 1), requests.map { it.page })
    }

    @Test
    fun loaderKnownIssueStaysOnAfterAServerRefusal() {
        coEvery { repo.getTransactions(any()) } returns BaseResponse(
            false, "Transactions", "err", TransactionsResponse(emptyList(), 1, 10, 0)
        )
        val vm = AllTransactionsViewModel(repo)
        main.scheduler.runCurrent()
        assertTrue(vm.uiState.value.responseState is ResponseState.Error)
        // Expected after a fix: false. Today isLoading stays true, which blocks every retry.
        assertTrue(vm.uiState.value.isLoading)
        vm.loadMoreTransactions()
        main.scheduler.runCurrent()
        coVerify(exactly = 1) { repo.getTransactions(any()) }
    }

    @Test
    fun loaderKnownIssueStaysOnAfterANetworkError() {
        coEvery { repo.getTransactions(any()) } throws IOException("offline")
        val vm = AllTransactionsViewModel(repo)
        main.scheduler.runCurrent()
        assertEquals(ResponseState.Error("offline"), vm.uiState.value.responseState)
        assertTrue(vm.uiState.value.isLoading)
    }
}
