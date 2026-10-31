package com.app.ezipaycoin.config

/** Pure rules that turn defaults, the server's answer and the developer toggle into the visible set. */
object FeatureResolver {

    /**
     * @param remote the `features` map from App/config, or null when it could not be fetched
     *   (404, offline, malformed). Missing keys fall back to the build default.
     * @param developerMode debug builds only: show everything, including unfinished features.
     */
    fun resolve(remote: Map<String, Boolean>?, developerMode: Boolean): Set<Feature> =
        Feature.entries.filter { feature ->
            when {
                developerMode -> true
                else -> {
                    val wanted = feature.serverKey?.let { remote?.get(it) } ?: feature.releaseDefault
                    wanted && feature.clientSupported
                }
            }
        }.toSet()

    /** Defaults for features with no server value, used before the first fetch finishes. */
    fun defaults(developerMode: Boolean): Set<Feature> = resolve(null, developerMode)
}
