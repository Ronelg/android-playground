package com.worldturtlemedia.playground.photos.list.filter

import androidx.fragment.app.activityViewModels
import com.worldturtlemedia.playground.common.base.ui.BaseFragment
import com.worldturtlemedia.playground.common.base.ui.dialog.showDialog
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.auth.ui.ConnectGooglePhotosDialog
import com.worldturtlemedia.playground.photos.auth.ui.PhotosAuthModel
import com.worldturtlemedia.playground.photos.databinding.ListFilterFragmentBinding
import com.worldturtlemedia.playground.photos.databinding.ListFilterFragmentBinding.bind

class ListFilterFragment : BaseFragment<ListFilterFragmentBinding>(R.layout.list_filter_fragment) {

    override val binding: ListFilterFragmentBinding by viewBinding { bind(it) }

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