package com.app.ezipaycoin.config

/**
 * A user-facing feature that can be switched off.
 *
 * [serverKey] is the key in `features` of `GET api/v1/App/config`; null means the feature has no
 * backend source and only a build decides. [releaseDefault] is what a release build shows until
 * (and unless) the server says otherwise. [clientSupported] is false while the app cannot drive
 * the backend flow even if the server turns it on, so a server switch alone never exposes a
 * screen that would fail.
 */
enum class Feature(
    val serverKey: String?,
    val releaseDefault: Boolean,
    val clientSupported: Boolean = true,
    val cacheLastKnown: Boolean = false,
) {
    /** Read-only staking pools. Stake/claim/unstake need the prepare -> sign flow, not in this client yet. */
    STAKING_POOLS("staking", releaseDefault = false),
    EARN_CHECKIN("earnCheckin", releaseDefault = true),
    EARN_TASKS("earnTasks", releaseDefault = true),
    // Reward claim has no flag: the backend route is off and the app has no claim UI.

    /** The client posts a different payload from what the backend expects (and the route is off). */
    KYC("kyc", releaseDefault = false, clientSupported = false),
    /** Backend mint is server-signed; the user must sign it. */
    NFT_MINT("nftMint", releaseDefault = false, clientSupported = false),

    /**
     * Google and Facebook sign-up on the set-up screen. App/config needs a signed-in user, so the
     * onboarding screen (before login) cannot ask the server: it uses the value stored from the
     * last successful fetch ([cacheLastKnown]), or the build default (off) when there is none.
     */
    SOCIAL_SIGN_IN("socialSignIn", releaseDefault = false, cacheLastKnown = true),

    // No backend source. Visible only in developer builds.
    SPIN_AND_WIN(null, releaseDefault = false, clientSupported = false),
    AIRDROPS(null, releaseDefault = false, clientSupported = false),
    LEADERBOARD(null, releaseDefault = false, clientSupported = false),
    PAY_UTILITIES(null, releaseDefault = false, clientSupported = false),
    PROFILE_PLACEHOLDERS(null, releaseDefault = false, clientSupported = false),
    HOME_PROMO(null, releaseDefault = false, clientSupported = false),
    ;
}
