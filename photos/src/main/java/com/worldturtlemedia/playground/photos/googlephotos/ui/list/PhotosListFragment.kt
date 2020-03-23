package com.worldturtlemedia.playground.photos.googlephotos.ui.list

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.worldturtlemedia.playground.common.base.ui.BaseFragment
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.common.ktx.navigate
import com.worldturtlemedia.playground.common.ktx.visibleOrGone
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState.*
import com.worldturtlemedia.playground.photos.auth.data.errorOrNull
import com.worldturtlemedia.playground.photos.auth.ui.PhotosAuthModel
import com.worldturtlemedia.playground.photos.databinding.PhotosListFragmentBinding
import com.worldturtlemedia.playground.photos.databinding.PhotosListFragmentBinding.bind

class PhotosListFragment : BaseFragment<PhotosListFragmentBinding>(R.layout.photos_list_fragment) {

    override val binding: PhotosListFragmentBinding by viewBinding { bind(it) }

    private val authViewModel: PhotosAuthModel by activityViewModels()

    private val viewModel: PhotosListModel by viewModels()

    override fun setupViews() = withBinding {
        toolbar.onFilterClicked = {
            navigate(PhotosListFragmentDirections.toFilterFragment())
        }

        mediaTypeFilter.onFilterClicked { viewModel.changeMediaType(it) }

        viewAuthError.onRetry = { authViewModel.showAuthDialogIfNeeded(this@PhotosListFragment) }

        viewUnauthenticated.onRetry = { authViewModel.showAuthDialogIfNeeded(this@PhotosListFragment) }
    }

    override fun observeViewModel() {
        authViewModel.init(requireContext())

        viewModel.state.observe(owner) { state ->
            binding.mediaTypeFilter.setSelected(state.mediaTypeFilter)
        }

        authViewModel.observe(owner) { state ->
            withBinding {
                viewAuthError.visibleOrGone = state.auth is Error
                state.auth.errorOrNull?.let { errorMessage ->
                    viewAuthError.binding.txtErrorMessage.text = errorMessage
                }

                viewUnauthenticated.visibleOrGone = !state.isShowingAuthDialog && state.auth is Unauthenticated

                authenticatedGroup.visibleOrGone = state.auth is Authenticated
            }
        }
    }
}