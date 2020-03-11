package com.worldturtlemedia.playground.photos.auth.data

import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed class GoogleAuthState {
    object Unauthenticated : GoogleAuthState()
    data class Error(val exception: Throwable) : GoogleAuthState()
    data class Authenticated(
        val serverCode: String,
        val user: GoogleAuthUser
    ) : GoogleAuthState()

    companion object {

        fun from(account: GoogleSignInAccount?): GoogleAuthState {
            if (account == null || account.isExpired) return Unauthenticated

            return try {
                Authenticated(
                    serverCode = account.serverAuthCode
                        ?: throw IllegalArgumentException("Server auth code was null!"),
                    user = GoogleAuthUser(
                        email = account.email
                            ?: throw IllegalArgumentException("Account email was null!"),
                        avatarUrl = account.photoUrl
                    )
                )
            } catch (error: Throwable) {
                Error(error)
            }
        }
    }
}

data class GoogleAuthUser(
    val email: String,
    val avatarUrl: Uri?
)

val GoogleAuthState.errorOrNull: String?
    get() = if (this is GoogleAuthState.Error) this.exception.message else null