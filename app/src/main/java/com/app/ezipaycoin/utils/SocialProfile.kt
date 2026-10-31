package com.app.ezipaycoin.utils

/** What a Google or Facebook account gave us, checked before a wallet is created from it. */
object SocialProfile {

    const val MISSING_EMAIL_MESSAGE =
        "This account did not share an email address. Use another account, or create a wallet without social sign-in."

    sealed class Result {
        data class Ready(val name: String, val email: String, val picture: String) : Result()
        data class Unusable(val message: String) : Result()
    }

    fun from(displayName: String?, email: String?, photoUrl: String?): Result {
        if (email.isNullOrBlank()) return Result.Unusable(MISSING_EMAIL_MESSAGE)
        val name = displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore('@')
        return Result.Ready(name, email, photoUrl.orEmpty())
    }
}
