package com.worldturtlemedia.playground.photos.auth.data

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.github.ajalt.timberkt.e
import com.github.florent37.inlineactivityresult.kotlin.InlineActivityResultException
import com.github.florent37.inlineactivityresult.kotlin.coroutines.startForResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.worldturtlemedia.playground.common.base.repository.StateRepository
import com.worldturtlemedia.playground.common.di.FakeDI
import com.worldturtlemedia.playground.photos.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/* FakeDI */
internal object GoogleAuthRepoFactory {
    val instance by lazy { GoogleAuthRepo(FakeDI.applicationContext) }
}

class GoogleAuthRepo(
    private val context: Context
) : StateRepository<GoogleAuthState>() {

    companion object {

        val SCOPE_PHOTOS_READONLY = Scope("https://www.googleapis.com/auth/photoslibrary.readonly")
    }

    override val debugMode: Boolean = true

    private val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestScopes(SCOPE_PHOTOS_READONLY)
        .requestIdToken(BuildConfig.GOOGLE_API_AUTH_CODE)
        .build()

    private val client by lazy { GoogleSignIn.getClient(context, signInOptions) }

    /**
     * This is called when created, but using the "injected" application context seems
     * to not get the proper auth state... so you have to call "refreshAuthState" from the
     * ViewModel/Fragment.
     */
    init {
        refreshAuthState(context)
    }

    /**
     * Get the current auth state from Google's library.
     */
    fun refreshAuthState(context: Context) {
        setState(getPreviousAuthState(context))
    }

    suspend fun signIn(activityRef: FragmentActivity) {
        if (currentState is GoogleAuthState.Authenticated) return

        val newState: GoogleAuthState? = try {
            val signInResultData = activityRef.startForResult(client.signInIntent).data
            val signInTask = GoogleSignIn.getSignedInAccountFromIntent(signInResultData)

            // No need to check the "success" or "complete" state of the "signInTask" as per:
            // https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignIn#public-static-taskgooglesigninaccount-getsignedinaccountfromintent-intent-data
            GoogleAuthState.from(signInTask.result)
        } catch (error: Throwable) {
            e(error) { "Sign in error" }

            if (error is InlineActivityResultException && error.resultCode == Activity.RESULT_CANCELED) null
            else GoogleAuthState.Error(error)
        }

        setState(newState)
    }

    suspend fun signOut() {
        suspendCancellableCoroutine<Unit> { continuation ->
            client.signOut().addOnCompleteListener { continuation.resume(Unit) }
        }

        setState(GoogleAuthState.Unauthenticated)
    }

    private fun getPreviousAuthState(context: Context): GoogleAuthState {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return GoogleAuthState.from(account)
    }
}




