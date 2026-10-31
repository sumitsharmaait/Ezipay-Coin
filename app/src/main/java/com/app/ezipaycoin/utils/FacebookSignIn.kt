package com.app.ezipaycoin.utils

import android.content.Context
import com.facebook.FacebookSdk

/**
 * The Facebook SDK is not started with the app (manifest: init provider removed, auto init, automatic
 * event logging and advertiser id collection off). It starts here, on the first Facebook sign-up tap,
 * and only when the social sign-in feature is on.
 */
object FacebookSignIn {

    @Suppress("DEPRECATION")
    fun ensureInitialized(context: Context) {
        val app = context.applicationContext
        if (!FacebookSdk.isInitialized()) FacebookSdk.sdkInitialize(app)
        if (!FacebookSdk.isFullyInitialized()) {
            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.fullyInitialize()
        }
    }
}
