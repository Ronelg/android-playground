package com.worldturtlemedia.playground.photos.auth.ui

import androidx.fragment.app.activityViewModels
import com.worldturtlemedia.playground.common.base.ui.dialog.SimpleDialog
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.common.ktx.observeProperty
import com.worldturtlemedia.playground.common.ktx.onClick
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import com.worldturtlemedia.playground.photos.databinding.ConnectGooglePhotosDialogBinding
import com.worldturtlemedia.playground.photos.databinding.ConnectGooglePhotosDialogBinding.bind

class ConnectGooglePhotosDialog : SimpleDialog<ConnectGooglePhotosDialogBinding>(
    R.layout.connect_google_photos_dialog
) {

    override val binding: ConnectGooglePhotosDialogBinding by viewBinding { bind(it) }

    override val isDismissible: Boolean = false

    private val authViewModel: PhotosAuthModel by activityViewModels()

    override fun setupViews() = withBinding {
        btnSignIn.onClick { authViewModel.signIn(requireActivity()) }
    }

    override fun initViewModel() {
        authViewModel.state.observeProperty(owner, { it.authEvent }) { authEvent ->
            authEvent?.consume { status ->
                when (status) {
                    is GoogleAuthState.Error -> close()
                    is GoogleAuthState.Authenticated -> confirm()
                }
            }
        }
    }
}