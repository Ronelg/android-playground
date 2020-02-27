package com.worldturtlemedia.playground.photos.list

import androidx.fragment.app.activityViewModels
import com.worldturtlemedia.playground.common.base.ui.BaseFragment
import com.worldturtlemedia.playground.common.base.ui.dialog.showDialog
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.auth.ui.ConnectGooglePhotosDialog
import com.worldturtlemedia.playground.photos.auth.ui.PhotosAuthModel
import com.worldturtlemedia.playground.photos.databinding.PhotosListFragmentBinding
import com.worldturtlemedia.playground.photos.databinding.PhotosListFragmentBinding.bind

class PhotosListFragment : BaseFragment<PhotosListFragmentBinding>(R.layout.photos_list_fragment) {

    override val binding: PhotosListFragmentBinding by viewBinding { bind(it) }

    private val authViewModel: PhotosAuthModel by activityViewModels()

    override fun setupViews() {
    }

    override fun observeViewModel() {
        if (!authViewModel.currentState.isAuthenticated) {
            showDialog(ConnectGooglePhotosDialog())
        }

        authViewModel.observe(owner) { state ->
            // If there is an error, display the error

            // If user is not authenticated display that state

            // Or display the list
        }

        // Have another viewmodel that fetches the data from GooglePhotos and displays the list
    }
}