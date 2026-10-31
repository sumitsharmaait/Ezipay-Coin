package com.app.ezipaycoin.config

/** Pure rules that turn defaults, the server's answer and the developer toggle into the visible set. */
object FeatureResolver {

    /**
     * @param remote the `features` map from App/config, or null when it could not be fetched
     *   (404, offline, malformed). When the map is present, a missing key means off.
     * @param developerMode debug builds only: show everything, including unfinished features.
     */
    fun resolve(remote: Map<String, Boolean>?, developerMode: Boolean): Set<Feature> =
        Feature.entries.filter { feature ->
            when {
                developerMode -> true
                else -> {
                    val wanted = when {
                        feature.serverKey == null -> feature.releaseDefault
                        remote == null -> feature.releaseDefault
                        // The server answered: a key it does not list is off.
                        else -> remote[feature.serverKey] ?: false
                    }
                    wanted && feature.clientSupported
                }
            }
        }.toSet()

    /** Defaults for features with no server value, used before the first fetch finishes. */
    fun defaults(developerMode: Boolean): Set<Feature> = resolve(null, developerMode)
}
