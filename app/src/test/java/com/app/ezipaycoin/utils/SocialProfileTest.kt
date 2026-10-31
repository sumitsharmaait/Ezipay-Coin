package com.app.ezipaycoin.utils

import com.app.ezipaycoin.utils.SocialProfile.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialProfileTest {

    @Test
    fun fullProfileIsReady() {
        assertEquals(Result.Ready("Ann", "ann@x.com", "http://p"), SocialProfile.from("Ann", "ann@x.com", "http://p"))
    }

    @Test
    fun missingEmailIsReportedWithAMessage() {
        for (email in listOf(null, "", "  ")) {
            val r = SocialProfile.from("Ann", email, null)
            assertTrue(r is Result.Unusable)
            assertEquals(SocialProfile.MISSING_EMAIL_MESSAGE, (r as Result.Unusable).message)
        }
    }

    @Test
    fun missingNameFallsBackToTheEmailPrefix() {
        assertEquals(Result.Ready("ann", "ann@x.com", ""), SocialProfile.from(null, "ann@x.com", null))
    }

    @Test
    fun missingPhotoIsEmptyNotTheWordNull() {
        assertEquals("", (SocialProfile.from("A", "a@b.c", null) as Result.Ready).picture)
    }
}
