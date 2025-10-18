package com.app.ezipaycoin.presentation.createwith

sealed class WalletSetupEvent {

    data class SocialDetailsFetched(
        val name: String,
        val email: String,
        val profilePic: String = ""
    ) : WalletSetupEvent()

    data object DismissDialog : WalletSetupEvent()

    data object SocialSignInLoading : WalletSetupEvent()



}
