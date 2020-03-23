package com.worldturtlemedia.playground.photos.auth.data

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.github.ajalt.timberkt.e
import com.github.florent37.inlineactivityresult.kotlin.InlineActivityResultException
import com.github.florent37.inlineactivityresult.kotlin.coroutines.startForResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.worldturtlemedia.playground.common.base.repository.StateRepository
import com.worldturtlemedia.playground.common.di.FakeDI
import com.worldturtlemedia.playground.photos.BuildConfig
import com.worldturtlemedia.playground.photos.auth.data.token.AccessTokenSource
import com.worldturtlemedia.playground.photos.auth.data.token.AccessTokens
import com.worldturtlemedia.playground.photos.db.PhotosSharedPref
import com.worldturtlemedia.playground.photos.db.retrieveTokens
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/* FakeDI */
internal object GoogleAuthRepoFactory {
    val instance by lazy {
        GoogleAuthRepo(
            FakeDI.applicationContext,
            PhotosSharedPref,
            AccessTokenSource.instance
        )
    }
}

class GoogleAuthRepo(
    context: Context,
    private val photosSharedPref: PhotosSharedPref,
    private val accessTokenSource: AccessTokenSource
) : StateRepository<GoogleAuthState>() {

    companion object {

        val SCOPE_PHOTOS_READONLY = Scope("https://www.googleapis.com/auth/photoslibrary.readonly")
    }

    override val debugMode: Boolean = true

    private var clientRef: GoogleSignInClient? = null

    /**
     * This is called when created, but using the "injected" application context seems
     * to not get the proper auth state... so you have to call "refreshAuthState" from the
     * ViewModel/Fragment.
     */
    init {
        launch { refreshAuthState(context) }
    }

    /**
     * Get the current auth state from Google's library.
     */
    suspend fun refreshAuthState(context: Context) {
        setState(getPreviousAuthState(context))
    }

    suspend fun signIn(activityRef: FragmentActivity) {
        if (currentState is GoogleAuthState.Authenticated) return

        val client = GoogleSignIn.getClient(activityRef, createSignInOptions())
            .also { clientRef = it }

        val newState: GoogleAuthState? = try {
            val signInResultData = activityRef.startForResult(client.signInIntent).data
            val signInTask = GoogleSignIn.getSignedInAccountFromIntent(signInResultData)

            val googleSignInAccount = signInTask.result
                ?: throw IllegalArgumentException("Sign-in result was null!")

            val serverAuthCode = googleSignInAccount.serverAuthCode
                ?: throw IllegalArgumentException("Server auth code was null!")

            val tokens = accessTokenSource.requestAccessToken(serverAuthCode)

            GoogleAuthState.from(signInTask.result, tokens.accessTokenIfValid)
        } catch (error: Throwable) {
            if (error is InlineActivityResultException && error.resultCode == Activity.RESULT_CANCELED) {
                e { "Sign in was cancelled" }
                null
            } else {
                e(error) { "Sign in error" }
                GoogleAuthState.Error(error)
            }
        }

        setState(newState)
    }

    suspend fun signOut() {
        if (clientRef == null) return

        suspendCancellableCoroutine<Unit> { continuation ->
            clientRef?.signOut()?.addOnCompleteListener { continuation.resume(Unit) }
        }

        setState(GoogleAuthState.Unauthenticated)
    }

    suspend fun getPreviousAuthState(context: Context): GoogleAuthState {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val accessTokens = getExistingOrNewToken(account)

        e { "TEST: serverAuthCode: ${account?.serverAuthCode}" }
        e { "TEST: account ${account?.email}" }
        e { "TEST: accessTokenIfValid: ${accessTokens?.accessTokenIfValid}"}

        return GoogleAuthState.from(account, accessTokens?.accessTokenIfValid)
    }

    private suspend fun getExistingOrNewToken(account: GoogleSignInAccount?): AccessTokens? {
        val existingToken = PhotosSharedPref.retrieveTokens()
        if (existingToken.hasValidToken) return existingToken

        return if (account == null) null
        else account.serverAuthCode?.let { authCode ->
            accessTokenSource.requestAccessToken(authCode)
        }
    }

    private fun createSignInOptions() =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(SCOPE_PHOTOS_READONLY)
            .requestServerAuthCode(BuildConfig.GOOGLE_API_CLIENT_ID).build()
}




