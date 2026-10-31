package com.app.ezipaycoin.presentation.createwith

sealed class WalletSetupEvent {

    data class SocialDetailsFetched(
        val name: String,
        val email: String,
        val profilePic: String = ""
    ) : WalletSetupEvent()

    /** The social account cannot be used (for example no email); [message] is shown to the user. */
    data class SocialDetailsUnavailable(val message: String) : WalletSetupEvent()

    data object DismissDialog : WalletSetupEvent()

    data object SocialSignInLoading : WalletSetupEvent()



}
