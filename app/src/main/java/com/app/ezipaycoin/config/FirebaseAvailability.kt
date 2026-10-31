package com.app.ezipaycoin.config

import android.content.Context
import com.app.ezipaycoin.BuildConfig
import com.google.firebase.FirebaseApp

/**
 * Whether Firebase (push, social sign-in) can be used in this process. False when the build has no
 * Firebase config for its application id ([BuildConfig.FIREBASE_ENABLED], set by Gradle) or when the
 * default FirebaseApp did not start. Callers skip the Firebase feature instead of crashing.
 */
object FirebaseAvailability {

    @Volatile
    private var available = false

    val isAvailable: Boolean get() = available

    /** Called once from Application.onCreate. */
    fun init(context: Context) {
        available = isAvailable(context)
    }

    fun isAvailable(context: Context): Boolean {
        if (!BuildConfig.FIREBASE_ENABLED) return false
        return runCatching {
            FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
        }.getOrDefault(false)
    }
}
