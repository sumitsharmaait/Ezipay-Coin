package com.app.ezipaycoin.presentation.pay

import com.app.ezipaycoin.presentation.dashboard.pay.recentPayees
import com.app.ezipaycoin.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentPayeesTest {
    private val own = Fixtures.OWN_ADDRESS
    private val other = Fixtures.RECIPIENT
    private val third = "0x813e9127c254fEd1Ed4c1003a1DA5eCD220Ee8CD"

    private fun sent(id: String, to: String?) = Fixtures.txItem(id = id, type = "send").copy(to = to, from = own)
    private fun received(id: String) = Fixtures.txItem(id = id, type = "receive").copy(to = own, from = other)

    @Test
    fun `received transfers never produce payees`() {
        assertTrue(recentPayees(listOf(received("a"), received("b")), own).isEmpty())
    }

    @Test
    fun `own address is excluded even on a sent row`() {
        assertTrue(recentPayees(listOf(sent("a", own.lowercase())), own).isEmpty())
    }

    @Test
    fun `payees are de-duplicated case-insensitively and keep order`() {
        val out = recentPayees(
            listOf(sent("1", other), received("2"), sent("3", other.lowercase()), sent("4", third), sent("5", null)),
            own
        )
        assertEquals(listOf("1", "4"), out.map { it.id })
    }
}
