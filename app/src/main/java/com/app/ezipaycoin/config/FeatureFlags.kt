package com.app.ezipaycoin.config

import android.content.Context
import com.app.ezipaycoin.BuildConfig
import com.app.ezipaycoin.domain.repository.AppConfigRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-wide feature switches. Starts from build defaults, then [refresh] applies the server's
 * `features` map. A failed refresh leaves the current values untouched.
 */
object FeatureFlags {

    private const val PREFS = "feature_flags"
    private const val KEY_DEV_MODE = "developer_mode"

    private var prefs: android.content.SharedPreferences? = null
    private var remote: Map<String, Boolean>? = null

    private val _enabled = MutableStateFlow(FeatureResolver.defaults(developerMode = false))
    val enabled: StateFlow<Set<Feature>> = _enabled.asStateFlow()

    private val _developerMode = MutableStateFlow(false)
    val developerMode: StateFlow<Boolean> = _developerMode.asStateFlow()

    /** Only debug builds can ever enable developer mode. */
    val developerModeAvailable: Boolean get() = BuildConfig.DEBUG

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = developerModeAvailable && runCatching { prefs?.getBoolean(KEY_DEV_MODE, false) }.getOrNull() == true
        _developerMode.value = stored
        recompute()
    }

    fun isEnabled(feature: Feature): Boolean = feature in _enabled.value

    fun setDeveloperMode(on: Boolean) {
        if (!developerModeAvailable) return
        _developerMode.value = on
        runCatching { prefs?.edit()?.putBoolean(KEY_DEV_MODE, on)?.apply() }
        recompute()
    }

    /** Returns true when the server answered; false leaves the current values in place. */
    suspend fun refresh(repository: AppConfigRepository): Boolean {
        val result = repository.getAppConfig()
        if (result !is ResponseState.Success) return false
        remote = result.data.features
        recompute()
        return true
    }

    internal fun applyRemoteForTest(features: Map<String, Boolean>?, developerMode: Boolean) {
        remote = features
        _developerMode.value = developerMode
        recompute()
    }

    private fun recompute() {
        _enabled.value = FeatureResolver.resolve(remote, _developerMode.value)
    }
}
