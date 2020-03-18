package com.worldturtlemedia.playground.photos.auth.data

import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed class GoogleAuthState {
    object Unauthenticated : GoogleAuthState()
    data class Error(val exception: Throwable) : GoogleAuthState()
    data class Authenticated(
        val user: GoogleAuthUser
    ) : GoogleAuthState() {

        fun getServerAuthCode(context: Context) =
            GoogleSignIn.getLastSignedInAccount(context)?.serverAuthCode
                ?: throw IllegalStateException("Authenticated, but there is no serverAuthCode!")
    }

    companion object {

        fun from(account: GoogleSignInAccount?): GoogleAuthState {
            if (account == null || account.isExpired) return Unauthenticated

            return try {
                Authenticated(
                    user = GoogleAuthUser(
                        id = account.id
                            ?: throw IllegalArgumentException("Account id was null!"),
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
    val id: String,
    val email: String,
    val avatarUrl: Uri?
)

val GoogleAuthState.errorOrNull: String?
    get() = if (this is GoogleAuthState.Error) exception.message else null