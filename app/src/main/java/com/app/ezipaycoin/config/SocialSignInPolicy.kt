package com.app.ezipaycoin.config

/** Decides whether the Google and Facebook sign-up buttons are shown. The flow code stays in place. */
object SocialSignInPolicy {

    /**
     * True only when [Feature.SOCIAL_SIGN_IN] is in the resolved set (server flag on, or developer
     * mode) and Firebase is available: both sign-in flows finish in Firebase Auth.
     */
    fun showButtons(enabled: Set<Feature>, firebaseAvailable: Boolean = true): Boolean =
        firebaseAvailable && Feature.SOCIAL_SIGN_IN in enabled
}
