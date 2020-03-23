package com.worldturtlemedia.playground.photos.auth.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.github.ajalt.timberkt.e
import com.worldturtlemedia.playground.common.base.ui.dialog.showDialog
import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.common.core.SingleEvent
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthRepoFactory
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotosAuthModel : StateViewModel<PhotosAuthState>(PhotosAuthState()) {

    private val googleAuthRepo = GoogleAuthRepoFactory.instance

    init {
        addStateSource(googleAuthRepo.state.asLiveData()) { auth ->
            copy(auth = auth, authEvent = SingleEvent(auth))
        }
    }

    fun init(context: Context) {
        viewModelScope.launch {
            googleAuthRepo.refreshAuthState(context)
        }
    }

    fun showAuthDialogIfNeeded(fragment: Fragment) {
        val test = googleAuthRepo.currentState
        e { "showAuthDialog: currentstate $test" }
        if (currentState.isAuthenticated) return

        val dialog = ConnectGooglePhotosDialog().onDismiss {
            setState { copy(isShowingAuthDialog = false) }
        }

        fragment.showDialog(dialog)
        setState { copy(isShowingAuthDialog = true) }
    }

    fun signIn(activity: FragmentActivity) = launchMain { googleAuthRepo.signIn(activity) }

    fun signOut() = launchMain { googleAuthRepo.signOut() }

    private fun launchMain(
        block: suspend CoroutineScope.() -> Unit
    ) = viewModelScope.launch(Dispatchers.Main, block = block)
}

data class PhotosAuthState(
    val auth: GoogleAuthState = GoogleAuthState.Unauthenticated,
    val authEvent: SingleEvent<GoogleAuthState>? = null,
    val isShowingAuthDialog: Boolean = false
) : State {

    val isAuthenticated: Boolean
        get() = auth is GoogleAuthState.Authenticated
}