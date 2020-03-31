package com.worldturtlemedia.playground.photos.googlephotos.ui.list

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.github.ajalt.timberkt.e
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
import com.worldturtlemedia.playground.photos.googlephotos.ui.list.items.MediaItemListHeader
import com.worldturtlemedia.playground.photos.googlephotos.ui.list.items.createMediaItemListItems
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section

class PhotosListFragment : BaseFragment<PhotosListFragmentBinding>(R.layout.photos_list_fragment) {

    companion object {
        const val NUMBER_OF_COLUMNS_PORTRAIT = 3
    }

    override val binding: PhotosListFragmentBinding by viewBinding { bind(it) }

    private val authViewModel: PhotosAuthModel by activityViewModels()

    private val viewModel: PhotosListModel by viewModels()

    private val listAdapter by lazy { GroupAdapter<GroupieViewHolder>() }

    override fun setupViews() = withBinding {
        toolbar.onFilterClicked = {
            navigate(PhotosListFragmentDirections.toFilterFragment())
        }

        toolbar.onDebugClicked = {
            navigate(PhotosListFragmentDirections.toDebugScreen())
        }

        mediaTypeFilter.onFilterClicked { viewModel.changeMediaType(it) }

        viewAuthError.onRetry = { authViewModel.showAuthDialogIfNeeded(this@PhotosListFragment) }

        viewUnauthenticated.onRetry =
            { authViewModel.showAuthDialogIfNeeded(this@PhotosListFragment) }

        setupRecyclerView()
    }

    override fun observeViewModel() {
        authViewModel.init(requireContext())

        viewModel.state.observe(owner) { state ->
            binding.mediaTypeFilter.setSelected(state.mediaFilter)
        }

        viewModel.observeProperty(owner, { it.items }) { items ->
            val start = System.currentTimeMillis()

            val mediaItemGroups = items.groupedByDate().map { (dateString, itemsForDay) ->
                Section().apply {
                    setHideWhenEmpty(true)
                    setHeader(MediaItemListHeader(dateString = dateString))
                    update(createMediaItemListItems(itemsForDay))
                }
            }

            listAdapter.updateAsync(mediaItemGroups) {
                val duration = (System.currentTimeMillis() - start) / 1000.0
                e { "Took $duration seconds to render ${items.size} items" }
            }
        }

        authViewModel.observe(owner) { state ->
            withBinding {
                viewAuthError.visibleOrGone = state.auth is Error
                state.auth.errorOrNull?.let { errorMessage ->
                    viewAuthError.binding.txtErrorMessage.text = errorMessage
                }

                viewUnauthenticated.visibleOrGone =
                    !state.isShowingAuthDialog && state.auth is Unauthenticated

                authenticatedGroup.visibleOrGone = state.auth is Authenticated
            }
        }
    }

    private fun setupRecyclerView() = with(binding.recyclerView) {
        layoutManager = createLayoutManager()
        adapter = listAdapter
    }

    private fun createLayoutManager() =
        GridLayoutManager(requireContext(), NUMBER_OF_COLUMNS_PORTRAIT).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (listAdapter.getItem(position) !is MediaItemListHeader) 1
                    else NUMBER_OF_COLUMNS_PORTRAIT
                }
            }
        }
}