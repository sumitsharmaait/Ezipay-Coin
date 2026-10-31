package com.app.ezipaycoin.presentation.profile

import com.app.ezipaycoin.data.remote.dto.response.MeProfileResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileMergeTest {
    private val cached = ProfileFields("Old Name", "old@x.com", "http://old/pic")

    @Test
    fun serverValuesOverrideCache() {
        val remote = MeProfileResponse("pk", "New", "new@x.com", "http://new/pic", "pending")
        assertEquals(ProfileFields("New", "new@x.com", "http://new/pic"), ProfileMerge.merge(cached, remote))
    }

    @Test
    fun blankOrMissingServerValuesKeepCache() {
        val remote = MeProfileResponse("pk", null, "  ", null, null)
        assertEquals(cached, ProfileMerge.merge(cached, remote))
    }

    @Test
    fun noResponseKeepsCache() {
        assertEquals(cached, ProfileMerge.merge(cached, null))
    }

    @Test
    fun emptyCacheTakesServer() {
        val remote = MeProfileResponse("pk", "A", "a@x.com", null, null)
        assertEquals(ProfileFields("A", "a@x.com", null), ProfileMerge.merge(ProfileFields(null, null, null), remote))
    }
}
