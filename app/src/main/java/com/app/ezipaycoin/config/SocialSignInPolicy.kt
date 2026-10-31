package com.app.ezipaycoin.config

/** Decides whether the Google and Facebook sign-up buttons are shown. The flow code stays in place. */
object SocialSignInPolicy {

    /** True only when [Feature.SOCIAL_SIGN_IN] is in the resolved set: server flag on (or developer mode). */
    fun showButtons(enabled: Set<Feature>): Boolean = Feature.SOCIAL_SIGN_IN in enabled
}
