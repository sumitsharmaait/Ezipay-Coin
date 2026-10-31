package com.app.ezipaycoin.data.remote.dto.response

/** `apiData` of `GET api/v1/App/config`. Every field may be absent on an older server. */
data class AppConfigResponse(
    val features: Map<String, Boolean>? = null,
    val minSupportedVersion: MinSupportedVersion? = null,
    val supportUrl: String? = null,
    val termsUrl: String? = null,
    val banners: List<AppBanner>? = null,
)

data class MinSupportedVersion(
    val android: String? = null,
    val ios: String? = null,
)

data class AppBanner(
    val id: Int = 0,
    val title: String? = null,
    val body: String? = null,
    val imageUrl: String? = null,
    val ctaLabel: String? = null,
    val ctaRoute: String? = null,
    val priority: Int = 0,
    val startsAtUtc: String? = null,
    val endsAtUtc: String? = null,
)
