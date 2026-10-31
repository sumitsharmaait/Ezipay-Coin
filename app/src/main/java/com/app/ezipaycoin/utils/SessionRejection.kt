package com.app.ezipaycoin.utils

object SessionRejection {
    /**
     * True for a 401 whose body is the session code `UN001`. A request that already followed an auth
     * challenge is not retried again, and the sign-in routes themselves are never retried.
     */
    fun shouldRecover(code: Int, hasPriorResponse: Boolean, path: String, body: () -> String): Boolean {
        if (code != 401 || hasPriorResponse) return false
        if (path.contains("/Auth/", ignoreCase = true)) return false
        return body().contains("UN001")
    }
}
