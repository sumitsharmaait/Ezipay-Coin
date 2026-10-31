package com.app.ezipaycoin.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialSignInTest {

    private fun shown(
        remote: Map<String, Boolean>?,
        developerMode: Boolean = false,
        lastKnown: Map<String, Boolean> = emptyMap(),
    ) = SocialSignInPolicy.showButtons(FeatureResolver.resolve(remote, developerMode, lastKnown))

    @Test
    fun `release default is off`() {
        assertFalse(Feature.SOCIAL_SIGN_IN.releaseDefault)
        assertFalse(shown(remote = null))
    }

    @Test
    fun `server key is socialSignIn`() {
        assertEquals("socialSignIn", Feature.SOCIAL_SIGN_IN.serverKey)
    }

    @Test
    fun `server on shows the buttons`() {
        assertTrue(shown(mapOf("socialSignIn" to true)))
    }

    @Test
    fun `server off, missing or unknown keeps them hidden`() {
        assertFalse(shown(mapOf("socialSignIn" to false)))
        assertFalse(shown(mapOf("staking" to true)))
        assertFalse(shown(emptyMap()))
    }

    @Test
    fun `before a fetch the last known server value decides`() {
        assertTrue(shown(remote = null, lastKnown = mapOf("socialSignIn" to true)))
        assertFalse(shown(remote = null, lastKnown = mapOf("socialSignIn" to false)))
        assertFalse(shown(remote = null, lastKnown = emptyMap()))
    }

    @Test
    fun `a fresh answer wins over the stored value`() {
        assertFalse(shown(remote = mapOf("earnCheckin" to true), lastKnown = mapOf("socialSignIn" to true)))
        assertTrue(shown(remote = mapOf("socialSignIn" to true), lastKnown = mapOf("socialSignIn" to false)))
    }

    @Test
    fun `stored values do not turn on other features`() {
        val set = FeatureResolver.resolve(null, false, mapOf("staking" to true, "kyc" to true))
        assertFalse(Feature.STAKING_POOLS in set)
        assertFalse(Feature.KYC in set)
    }

    @Test
    fun `developer mode shows the buttons`() {
        assertTrue(shown(remote = null, developerMode = true))
    }

    @Test
    fun `buttons are hidden when the feature is not in the enabled set`() {
        assertFalse(SocialSignInPolicy.showButtons(emptySet()))
        assertTrue(SocialSignInPolicy.showButtons(setOf(Feature.SOCIAL_SIGN_IN)))
    }
}
