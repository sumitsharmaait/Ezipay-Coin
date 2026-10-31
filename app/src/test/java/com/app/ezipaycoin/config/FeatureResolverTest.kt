package com.app.ezipaycoin.config

import com.app.ezipaycoin.data.remote.dto.response.AppConfigResponse
import com.app.ezipaycoin.domain.repository.AppConfigRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureResolverTest {

    @Test
    fun `release defaults show only verified features`() {
        val shown = FeatureResolver.resolve(remote = null, developerMode = false)
        assertEquals(setOf(Feature.EARN_CHECKIN, Feature.EARN_TASKS), shown)
    }

    @Test
    fun `absent route keeps defaults`() {
        assertEquals(
            FeatureResolver.defaults(false),
            FeatureResolver.resolve(remote = null, developerMode = false)
        )
    }

    @Test
    fun `server can turn staking pools on and checkin off`() {
        val shown = FeatureResolver.resolve(mapOf("staking" to true, "earnCheckin" to false), false)
        assertTrue(Feature.STAKING_POOLS in shown)
        assertFalse(Feature.EARN_CHECKIN in shown)
        assertTrue(Feature.EARN_TASKS in shown)
    }

    @Test
    fun `unknown and missing keys fall back to defaults`() {
        val shown = FeatureResolver.resolve(mapOf("somethingNew" to true), false)
        assertEquals(setOf(Feature.EARN_CHECKIN, Feature.EARN_TASKS), shown)
    }

    @Test
    fun `server cannot expose a feature the client cannot drive`() {
        val shown = FeatureResolver.resolve(
            mapOf("kyc" to true, "nftMint" to true), false
        )
        assertFalse(Feature.KYC in shown)
        assertFalse(Feature.NFT_MINT in shown)
    }

    @Test
    fun `features without a server key never come from the server`() {
        val shown = FeatureResolver.resolve(mapOf("spinAndWin" to true), false)
        assertFalse(Feature.SPIN_AND_WIN in shown)
    }

    @Test
    fun `developer mode shows everything`() {
        assertEquals(Feature.entries.toSet(), FeatureResolver.resolve(null, developerMode = true))
    }

    @Test
    fun `refresh applies the server map and failure leaves it alone`() = runTest {
        FeatureFlags.applyRemoteForTest(null, developerMode = false)
        val ok = object : AppConfigRepository {
            override suspend fun getAppConfig() =
                ResponseState.Success(AppConfigResponse(features = mapOf("staking" to true)))
        }
        assertTrue(FeatureFlags.refresh(ok))
        assertTrue(FeatureFlags.isEnabled(Feature.STAKING_POOLS))

        val failing = object : AppConfigRepository {
            override suspend fun getAppConfig() = ResponseState.Error("NOT_FOUND")
        }
        assertFalse(FeatureFlags.refresh(failing))
        assertTrue(FeatureFlags.isEnabled(Feature.STAKING_POOLS))
        FeatureFlags.applyRemoteForTest(null, developerMode = false)
    }
}
