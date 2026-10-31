package com.app.ezipaycoin.presentation.profile

import com.app.ezipaycoin.data.remote.dto.response.MeProfileResponse

/** Profile fields shown on screen and cached in the preferences store. */
data class ProfileFields(val name: String?, val email: String?, val profile: String?)

object ProfileMerge {
    /** Server values win when present; blank or missing ones keep the cached value. */
    fun merge(cached: ProfileFields, remote: MeProfileResponse?): ProfileFields {
        if (remote == null) return cached
        return ProfileFields(
            name = remote.name.takeUnless { it.isNullOrBlank() } ?: cached.name,
            email = remote.email.takeUnless { it.isNullOrBlank() } ?: cached.email,
            profile = remote.profilePic.takeUnless { it.isNullOrBlank() } ?: cached.profile
        )
    }
}
