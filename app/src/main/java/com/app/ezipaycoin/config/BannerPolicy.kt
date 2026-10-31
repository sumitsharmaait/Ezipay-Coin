package com.app.ezipaycoin.config

import com.app.ezipaycoin.data.remote.dto.response.AppBanner
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/** Where a banner button can lead inside the app. */
enum class BannerTarget { HOME, WALLET, PAY, RECEIVE, HISTORY, EARN, LEARN, PROFILE }

object BannerPolicy {

    const val MAX_BANNERS = 3

    /**
     * Banners to show: titled, inside their window, lowest priority number first.
     * The server already filters; this guards against clock skew and bad rows.
     */
    fun select(banners: List<AppBanner>, now: Instant, max: Int = MAX_BANNERS): List<AppBanner> =
        banners
            .filter { !it.title.isNullOrBlank() }
            .filter { inWindow(it, now) }
            .sortedWith(compareBy({ it.priority }, { it.id }))
            .take(max)

    /**
     * The in-app destination for a banner's `ctaRoute`, or null when the button must be hidden:
     * no label, unknown or unsupported route, or its feature is off.
     */
    fun resolveCta(banner: AppBanner, enabled: Set<Feature>): BannerTarget? {
        if (banner.ctaLabel.isNullOrBlank()) return null
        return when (banner.ctaRoute?.trim()) {
            "home" -> BannerTarget.HOME
            "wallet", "nft" -> BannerTarget.WALLET
            "send" -> BannerTarget.PAY
            "receive" -> BannerTarget.RECEIVE
            "history" -> BannerTarget.HISTORY
            "learn" -> BannerTarget.LEARN
            "profile" -> BannerTarget.PROFILE
            "earn" -> BannerTarget.EARN.takeIf { Feature.EARN_CHECKIN in enabled || Feature.EARN_TASKS in enabled }
            "earn.checkin" -> BannerTarget.EARN.takeIf { Feature.EARN_CHECKIN in enabled }
            "earn.tasks" -> BannerTarget.EARN.takeIf { Feature.EARN_TASKS in enabled }
            "staking" -> BannerTarget.EARN.takeIf { Feature.STAKING_POOLS in enabled }
            // swap, kyc, virtualcard, notifications, settings, support: no screen in this app.
            else -> null
        }
    }

    /** Only https images load; anything else is dropped. */
    fun safeImageUrl(banner: AppBanner): String? = banner.imageUrl?.takeIf { it.startsWith("https://") }

    private fun inWindow(banner: AppBanner, now: Instant): Boolean {
        val start = parse(banner.startsAtUtc)
        val end = parse(banner.endsAtUtc)
        return (start == null || !now.isBefore(start)) && (end == null || now.isBefore(end))
    }

    private fun parse(text: String?): Instant? {
        if (text.isNullOrBlank()) return null
        return runCatching { OffsetDateTime.parse(text).toInstant() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(text).toInstant(ZoneOffset.UTC) }.getOrNull()
    }
}
