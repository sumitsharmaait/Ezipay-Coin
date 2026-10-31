package com.app.ezipaycoin.config

import com.app.ezipaycoin.data.remote.dto.response.AppBanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class BannerPolicyTest {

    private val now = Instant.parse("2026-10-31T12:00:00Z")

    private fun banner(
        id: Int = 1, title: String? = "T", priority: Int = 0, label: String? = "Go", route: String? = null,
        starts: String? = null, ends: String? = null, image: String? = null,
    ) = AppBanner(id, title, null, image, label, route, priority, starts, ends)

    @Test
    fun `empty list shows nothing`() = assertEquals(emptyList<AppBanner>(), BannerPolicy.select(emptyList(), now))

    @Test
    fun `orders by priority then id and caps the count`() {
        val list = listOf(banner(4, priority = 2), banner(3, priority = 1), banner(2, priority = 1), banner(1, priority = 0))
        assertEquals(listOf(1, 2, 3), BannerPolicy.select(list, now).map { it.id })
    }

    @Test
    fun `drops untitled and out of window banners`() {
        val list = listOf(
            banner(1, title = " "),
            banner(2, starts = "2026-11-01T00:00:00Z"),
            banner(3, ends = "2026-10-31T11:00:00Z"),
            banner(4, starts = "2026-10-31T00:00:00", ends = "2026-11-01T00:00:00Z"),
            banner(5, starts = "garbage"),
        )
        assertEquals(listOf(4, 5), BannerPolicy.select(list, now).map { it.id })
    }

    @Test
    fun `zone-less dates are read as UTC`() {
        // now is 12:00Z; a zone-less end of 11:59:59 is already over, 12:00:01 is not.
        val over = banner(1, ends = "2026-10-31T11:59:59")
        val open = banner(2, ends = "2026-10-31T12:00:01.1234567")
        val notYet = banner(3, starts = "2026-10-31T12:00:01")
        assertEquals(listOf(2), BannerPolicy.select(listOf(over, open, notYet), now).map { it.id })
    }

    @Test
    fun `Z and offset dates give the same result as zone-less UTC`() {
        val z = banner(1, starts = "2026-10-31T00:00:00Z", ends = "2026-10-31T12:00:01Z")
        val offset = banner(2, starts = "2026-10-31T02:00:00+02:00", ends = "2026-10-31T14:00:01+02:00")
        val bare = banner(3, starts = "2026-10-31T00:00:00", ends = "2026-10-31T12:00:01")
        assertEquals(listOf(1, 2, 3), BannerPolicy.select(listOf(z, offset, bare), now).map { it.id })
        val expired = listOf(
            banner(4, ends = "2026-10-31T12:00:00Z"),
            banner(5, ends = "2026-10-31T14:00:00+02:00"),
            banner(6, ends = "2026-10-31T12:00:00"),
        )
        assertEquals(emptyList<Int>(), BannerPolicy.select(expired, now).map { it.id })
    }

    @Test
    fun `cta follows the destination feature`() {
        val on = setOf(Feature.EARN_CHECKIN, Feature.EARN_TASKS)
        assertEquals(BannerTarget.EARN, BannerPolicy.resolveCta(banner(route = "earn"), on))
        assertNull(BannerPolicy.resolveCta(banner(route = "earn.checkin"), setOf(Feature.EARN_TASKS)))
        assertNull(BannerPolicy.resolveCta(banner(route = "staking"), on))
        assertEquals(BannerTarget.EARN, BannerPolicy.resolveCta(banner(route = "staking"), on + Feature.STAKING_POOLS))
    }

    @Test
    fun `plain destinations resolve without a feature`() {
        val none = emptySet<Feature>()
        assertEquals(BannerTarget.WALLET, BannerPolicy.resolveCta(banner(route = "wallet"), none))
        assertEquals(BannerTarget.PAY, BannerPolicy.resolveCta(banner(route = "send"), none))
        assertEquals(BannerTarget.HISTORY, BannerPolicy.resolveCta(banner(route = "history"), none))
        assertEquals(BannerTarget.LEARN, BannerPolicy.resolveCta(banner(route = "learn"), none))
    }

    @Test
    fun `unknown unsupported or label-less routes hide the button`() {
        val all = Feature.entries.toSet()
        assertNull(BannerPolicy.resolveCta(banner(route = "kyc"), all))
        assertNull(BannerPolicy.resolveCta(banner(route = "swap"), all))
        assertNull(BannerPolicy.resolveCta(banner(route = "somethingnew"), all))
        assertNull(BannerPolicy.resolveCta(banner(route = null), all))
        assertNull(BannerPolicy.resolveCta(banner(route = "wallet", label = null), all))
    }

    @Test
    fun `only https images are used`() {
        assertEquals("https://x/y.png", BannerPolicy.safeImageUrl(banner(image = "https://x/y.png")))
        assertNull(BannerPolicy.safeImageUrl(banner(image = "http://x/y.png")))
        assertNull(BannerPolicy.safeImageUrl(banner(image = null)))
    }
}
