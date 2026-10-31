package com.app.ezipaycoin.utils

import com.app.ezipaycoin.config.FirebaseAvailability
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object FcmToken {
    /** Waits for the push token; an empty string when none is available, so sign-in still goes ahead. */
    suspend fun fetch(): String {
        if (!FirebaseAvailability.isAvailable) return ""
        return request()
    }

    private suspend fun request(): String = suspendCancellableCoroutine { cont ->
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                val token = if (task.isSuccessful) task.result.orEmpty() else ""
                if (cont.isActive) cont.resume(token)
            }
        } catch (e: Exception) {
            if (cont.isActive) cont.resume("")
        }
    }
}
