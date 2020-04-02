package com.worldturtlemedia.playground.photos.auth.data

import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.auth.oauth2.AccessToken

sealed class GoogleAuthState {
    object Unauthenticated : GoogleAuthState()
    data class Error(val exception: Throwable) : GoogleAuthState()
    data class Authenticated(
        val user: GoogleAuthUser,
        val accessToken: AccessToken
    ) : GoogleAuthState()

    companion object {

        fun from(account: GoogleSignInAccount?, token: AccessToken?): GoogleAuthState {
            if (account == null || token == null) return Unauthenticated

            return try {
                Authenticated(
                    user = GoogleAuthUser.from(account),
                    accessToken = token
                )
            } catch (error: Throwable) {
                Error(error)
            }
        }
    }
}

data class GoogleAuthUser(
    val id: String,
    val email: String,
    val avatarUrl: Uri?
) {

    companion object {

        fun from(account: GoogleSignInAccount) = GoogleAuthUser(
            id = account.id!!,
            email = account.email!!,
            avatarUrl = account.photoUrl
        )
    }
}

val GoogleAuthState.errorOrNull: String?
    get() = if (this is GoogleAuthState.Error) exception.message else null