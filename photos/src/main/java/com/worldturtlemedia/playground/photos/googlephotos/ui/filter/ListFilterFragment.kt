package com.worldturtlemedia.playground.photos.googlephotos.ui.filter

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.worldturtlemedia.playground.common.base.ui.BaseFragment
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.common.ktx.onClick
import com.worldturtlemedia.playground.common.ktx.visibleOrGone
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import com.worldturtlemedia.playground.photos.auth.ui.PhotosAuthModel
import com.worldturtlemedia.playground.photos.databinding.ListFilterFragmentBinding
import com.worldturtlemedia.playground.photos.databinding.ListFilterFragmentBinding.bind

class ListFilterFragment : BaseFragment<ListFilterFragmentBinding>(R.layout.list_filter_fragment) {

    private val authViewModel: PhotosAuthModel by activityViewModels()

    private val viewModel: ListFilterModel by viewModels()

    override val binding: ListFilterFragmentBinding by viewBinding { bind(it) }

    override fun setupViews() = withBinding {
        btnClose.onClick { viewModel.close() }
        btnClearAll.onClick { viewModel.clearFilters() }
        btnApply.onClick { viewModel.applyFilters() }

        viewAuthUser.setOnDisconnect { authViewModel.signOut() }
    }

    override fun observeViewModel() {
        authViewModel.state.observe(owner) { state ->
            withBinding {
                viewAuthUser.visibleOrGone = state.isAuthenticated

                if (state.auth is GoogleAuthState.Authenticated) {
                    val authUser = state.auth.user
                    viewAuthUser.updateInfo(authUser.email, authUser.avatarUrl)
                }
            }
        }

        viewModel.observe(owner) { state ->
            with(binding.btnApply) {
                isEnabled = state.filters.isNotEmpty()

                val size = state.filters.size
                text = if (size <= 0) getString(R.string.no_filters_selected)
                else resources.getQuantityString(R.plurals.apply_filter, size, size)
            }

            state.event?.consume(::handleModelEvent)
        }
    }

    private fun handleModelEvent(event: ListFilterEvent) {
        when (event) {
            is ListFilterEvent.Close -> findNavController().popBackStack()
            is ListFilterEvent.Apply -> findNavController().popBackStack()
        }
    }
}