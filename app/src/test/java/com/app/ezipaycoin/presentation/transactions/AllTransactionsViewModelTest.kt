package com.app.ezipaycoin.presentation.transactions

import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.request.EstimateGasRequest
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.BnbChainResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionReceiptResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.domain.repository.TransactionsRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AllTransactionsViewModelTest {

    private fun item(id: String, type: String = "send") = TransactionsResponse.TransactionsItem(
        id = id, chain = "BSC", type = type, symbol = "EZP", amount = 1.0, fiatValue = "1",
        timestamp = "2026-10-31T10:00:00Z", status = "ok", to = "0xto", from = "0xfrom",
        nonce = null, gas = "0", gasPrice = "0", gasUsed = "0", cumulativeGasUsed = "0"
    )

    private fun ok(items: List<TransactionsResponse.TransactionsItem>, total: Int) =
        BaseResponse(true, "ok", "", TransactionsResponse(items, 1, 10, total))

    private class FakeRepo : TransactionsRepository {
        val requests = mutableListOf<TransactionsRequest>()
        val script = ArrayDeque<() -> BaseResponse<TransactionsResponse>>()
        override suspend fun getTransactions(request: TransactionsRequest): BaseResponse<TransactionsResponse> {
            requests += request
            return script.removeFirst().invoke()
        }
        override suspend fun payMoney(request: BitPayRequest): BnbChainResponse = error("unused")
        override suspend fun getTransactionReceipt(request: BitPayRequest): TransactionReceiptResponse = error("unused")
        override suspend fun getTransactionCount(request: BitPayRequest): BnbChainResponse = error("unused")
        override suspend fun getEstimateGas(request: EstimateGasRequest): BnbChainResponse = error("unused")
    }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private fun page(prefix: String, n: Int = 10) = (1..n).map { item("$prefix$it") }

    @Test
    fun pagesAdvanceFromOneAndStopWhenTheTotalIsReached() {
        val repo = FakeRepo()
        repo.script += { ok(page("a"), 15) }
        repo.script += { ok(page("b", 5), 15) }
        val vm = AllTransactionsViewModel(repo)
        assertEquals(1, repo.requests[0].page)
        assertEquals(10, vm.uiState.value.transactionsList.size)
        assertTrue(vm.uiState.value.canLoadMore)

        vm.loadMoreTransactions()
        assertEquals(2, repo.requests[1].page)
        assertEquals(15, vm.uiState.value.transactionsList.size)
        assertFalse(vm.uiState.value.canLoadMore)
        assertFalse(vm.uiState.value.isLoading)

        vm.loadMoreTransactions()
        assertEquals(2, repo.requests.size)
    }

    @Test
    fun aShortFirstPageEndsPaging() {
        val repo = FakeRepo()
        repo.script += { ok(page("a", 3), 0) }
        val vm = AllTransactionsViewModel(repo)
        assertFalse(vm.uiState.value.canLoadMore)
    }

    @Test
    fun serverRefusalResetsLoadingAndRetryAsksForTheSamePage() {
        val repo = FakeRepo()
        repo.script += { BaseResponse(false, "boom", "", TransactionsResponse(emptyList(), 0, 0, 0)) }
        repo.script += { ok(page("a"), 20) }
        val vm = AllTransactionsViewModel(repo)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals("boom", (vm.uiState.value.responseState as ResponseState.Error).message)

        vm.retry()
        assertEquals(listOf(1, 1), repo.requests.map { it.page })
        assertEquals(10, vm.uiState.value.transactionsList.size)
        assertTrue(vm.uiState.value.responseState is ResponseState.Success<*>)
    }

    @Test
    fun exceptionResetsLoadingAndKeepsEarlierRows() {
        val repo = FakeRepo()
        repo.script += { ok(page("a"), 30) }
        repo.script += { throw java.io.IOException("offline") }
        repo.script += { ok(page("b"), 30) }
        val vm = AllTransactionsViewModel(repo)
        vm.loadMoreTransactions()
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(10, vm.uiState.value.transactionsList.size)
        assertTrue(vm.uiState.value.responseState is ResponseState.Error)

        vm.retry()
        assertEquals(listOf(1, 2, 2), repo.requests.map { it.page })
        assertEquals(20, vm.uiState.value.transactionsList.size)
    }

    @Test
    fun filterChangeRestartsAtPageOne() {
        val repo = FakeRepo()
        repo.script += { ok(page("a"), 30) }
        repo.script += { ok(page("b"), 30) }
        repo.script += { ok(page("c", 2), 2) }
        val vm = AllTransactionsViewModel(repo)
        vm.loadMoreTransactions()
        vm.onEvent(TransactionsEvent.FilterChanged("Received"))
        assertEquals(1, repo.requests[2].page)
        assertEquals("received", repo.requests[2].type)
        assertEquals(2, vm.uiState.value.transactionsList.size)
    }

    @Test
    fun counterpartyIsTheOtherSide() {
        assertEquals("0xto", item("1", "send").counterparty())
        assertEquals("0xfrom", item("2", "received").counterparty())
    }
}
