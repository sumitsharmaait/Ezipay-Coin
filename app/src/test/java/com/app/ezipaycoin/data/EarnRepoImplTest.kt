package com.app.ezipaycoin.data

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.repository.AppConfigRepoImpl
import com.app.ezipaycoin.data.repository.EarnRepoImpl
import com.app.ezipaycoin.data.repository.LearnRepoImpl
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Golden JSON below mirrors what the .NET API emits (camelCase, four-decimal amount strings). */
class EarnRepoImplTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService
    private lateinit var earn: EarnRepoImpl

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        earn = EarnRepoImpl(api, chainId = 56)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun json(body: String, code: Int = 200) =
        server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body))

    private fun envelope(data: String, status: Boolean = true, message: String = "ok") =
        """{"apiStatus":$status,"apiMessage":"$message","error":null,"apiData":$data}"""

    @Test
    fun `summary sends status and chain id and parses the total`() = runTest {
        json(envelope("""{"tokenSymbol":"EZP","status":"CLAIMABLE","totalAmount":12.5,"itemsCount":3}"""))
        val result = earn.summary("CLAIMABLE")
        val req = server.takeRequest()
        assertEquals("/api/v1/Earn/summary?status=CLAIMABLE&chainId=56", req.path)
        result as ResponseState.Success
        assertEquals(12.5, result.data.totalAmount!!, 0.0)
        assertEquals("EZP", result.data.tokenSymbol)
    }

    @Test
    fun `check-in status parses dates streak and next reward`() = runTest {
        json(
            envelope(
                """{"month":"2026-10","today":"2026-10-31","checkedInDates":["2026-10-30"],"canCheckIn":true,
                |"currentStreak":1,"nextReward":{"amount":"2.0000","tokenSymbol":"EZP","lockDays":7}}""".trimMargin()
            )
        )
        val r = earn.checkInStatus("2026-10") as ResponseState.Success
        assertEquals("/api/v1/Earn/checkin/status?month=2026-10", server.takeRequest().path)
        assertEquals(listOf("2026-10-30"), r.data.checkedInDates)
        assertTrue(r.data.canCheckIn)
        assertEquals(7, r.data.nextReward!!.lockDays)
    }

    @Test
    fun `check-in sends the date and an idempotency key`() = runTest {
        json(
            envelope(
                """{"checkedInDate":"2026-10-31","newStreak":2,"reward":{"amount":"2.0000","tokenSymbol":"EZP",
                |"status":"LOCKED","lockedUntil":"2026-11-07T00:00:00","ledgerId":"6f1f4a3e-8f0c-4a2b-9c1d-0a1b2c3d4e5f"}}""".trimMargin()
            )
        )
        val r = earn.checkIn("2026-10-31", "key-1") as ResponseState.Success
        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/api/v1/Earn/checkin?date=2026-10-31", req.path)
        assertEquals("key-1", req.getHeader("Idempotency-Key"))
        assertEquals(2, r.data.newStreak)
        assertEquals("6f1f4a3e-8f0c-4a2b-9c1d-0a1b2c3d4e5f", r.data.reward!!.ledgerId)
    }

    @Test
    fun `a refused request keeps the server code`() = runTest {
        json(envelope("null", status = false, message = "ALREADY_CHECKED_IN"))
        val r = earn.checkIn("2026-10-31", "k")
        assertEquals(ResponseState.Error("ALREADY_CHECKED_IN"), r)
    }

    @Test
    fun `tasks parse with reward and status`() = runTest {
        json(
            envelope(
                """{"date":"2026-10-31","tasks":[{"taskId":4,"type":"VIDEO","title":"Watch","description":"d",
                |"requiredSeconds":30,"contentUrl":"https://example.com/v","reward":{"amount":"1.0000","tokenSymbol":"EZP"},
                |"status":"PENDING"},{"taskId":5,"type":"LINK","title":"Read","description":null,"requiredSeconds":null,
                |"contentUrl":null,"reward":{"amount":"0.5000","tokenSymbol":"EZP"},"status":"COMPLETED"}]}""".trimMargin()
            )
        )
        val r = earn.tasks("2026-10-31") as ResponseState.Success
        assertEquals("/api/v1/Earn/checkin/tasks?date=2026-10-31", server.takeRequest().path)
        assertEquals(2, r.data.tasks!!.size)
        assertFalse(r.data.tasks!![0].isCompleted)
        assertTrue(r.data.tasks!![1].isCompleted)
        assertEquals(30, r.data.tasks!![0].requiredSeconds)
    }

    @Test
    fun `complete task posts date and watched seconds with a key`() = runTest {
        json(
            envelope(
                """{"taskId":4,"date":"2026-10-31","taskStatus":"COMPLETED","reward":{"amount":"1.0000",
                |"tokenSymbol":"EZP","status":"CLAIMABLE","ledgerId":"6f1f4a3e-8f0c-4a2b-9c1d-0a1b2c3d4e5f"}}""".trimMargin()
            )
        )
        val r = earn.completeTask(4, "2026-10-31", 31, "key-2") as ResponseState.Success
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/api/v1/Earn/checkin/tasks/4/complete", req.path)
        assertEquals("key-2", req.getHeader("Idempotency-Key"))
        assertEquals("""{"date":"2026-10-31","watchedSeconds":31}""", req.body.readUtf8())
        assertEquals("COMPLETED", r.data.taskStatus)
    }

    @Test
    fun `staking pools parse`() = runTest {
        json(
            envelope(
                """[{"poolId":1,"name":"Flexible","apr":8.5,"lockDays":30,"minStake":"100","isActive":true,
                |"token":{"symbol":"EZP","address":"0xabc"},"stakingContract":"0xdef"}]""".trimMargin()
            )
        )
        val r = earn.stakingPools() as ResponseState.Success
        assertEquals("/api/v1/earn/Staking/pools?chainId=56", server.takeRequest().path)
        assertEquals(8.5, r.data[0].apr!!, 0.0)
        assertEquals("EZP", r.data[0].token!!.symbol)
    }

    @Test
    fun `learn list parses and an empty list is success`() = runTest {
        json(
            envelope(
                """[{"id":1,"title":"Intro","subtitle":"Setup","icon":null,"type":"video",
                |"resourceUrl":"https://example.com/a","language":"en","isFeatured":true,"sortOrder":1,"status":"active"}]""".trimMargin()
            )
        )
        json(envelope("[]"))
        val learn = LearnRepoImpl(api)
        val first = learn.items() as ResponseState.Success
        assertEquals("/api/v1/learn", server.takeRequest().path)
        assertEquals("Intro", first.data[0].title)
        assertEquals(emptyList<Any>(), (learn.items() as ResponseState.Success).data)
    }

    @Test
    fun `app config parses features and banners`() = runTest {
        json(
            envelope(
                """{"features":{"staking":false,"earnCheckin":true},"minSupportedVersion":{"android":"1.1.0","ios":null},
                |"supportUrl":null,"termsUrl":"https://example.com/t","banners":[{"id":9,"title":"Hi","body":null,
                |"imageUrl":null,"ctaLabel":"Go","ctaRoute":"earn","priority":1,"startsAtUtc":null,"endsAtUtc":null}]}""".trimMargin()
            )
        )
        val r = AppConfigRepoImpl(api).getAppConfig() as ResponseState.Success
        assertEquals("/api/v1/App/config", server.takeRequest().path)
        assertEquals(true, r.data.features!!["earnCheckin"])
        assertEquals("earn", r.data.banners!![0].ctaRoute)
        assertNotNull(r.data.minSupportedVersion)
    }

    @Test
    fun `a 404 from an older server becomes NOT_FOUND`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        assertEquals(ResponseState.Error("NOT_FOUND"), AppConfigRepoImpl(api).getAppConfig())
    }

    @Test
    fun `a 401 asks the user to unlock again`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        val r = earn.summary("LOCKED") as ResponseState.Error
        assertTrue(r.message.contains("session has expired"))
    }
}
