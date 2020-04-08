package com.worldturtlemedia.playground.photos.googlephotos.ui.list

import android.os.Parcelable
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.github.ajalt.timberkt.e
import com.worldturtlemedia.playground.common.base.ui.BaseFragment
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.common.ktx.*
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState.*
import com.worldturtlemedia.playground.photos.auth.data.errorOrNull
import com.worldturtlemedia.playground.photos.auth.ui.PhotosAuthModel
import com.worldturtlemedia.playground.photos.auth.ui.PhotosAuthState
import com.worldturtlemedia.playground.photos.databinding.PhotosListFragmentBinding
import com.worldturtlemedia.playground.photos.databinding.PhotosListFragmentBinding.bind
import com.worldturtlemedia.playground.photos.googlephotos.ui.Constants.NUMBER_OF_COLUMNS
import com.worldturtlemedia.playground.photos.googlephotos.ui.list.items.MediaItemListAdapter
import com.worldturtlemedia.playground.photos.googlephotos.ui.list.items.MediaItemListItem
import com.worldturtlemedia.playground.photos.googlephotos.ui.util.BidirectionalInfiniteScrollListener
import kotlinx.android.parcel.Parcelize
import org.joda.time.LocalDate

@Parcelize
data class PhotoListArgs(
    val selectedDate: LocalDate
) : Parcelable {

    companion object {
        val default = PhotoListArgs(selectedDate = LocalDate.now())
    }
}

class PhotosListFragment : BaseFragment<PhotosListFragmentBinding>(R.layout.photos_list_fragment) {

    override val binding: PhotosListFragmentBinding by viewBinding { bind(it) }

    private val authViewModel: PhotosAuthModel by activityViewModels()

    private val viewModel: PhotosListModel by viewModels()

    private val navArgs by navArgs<PhotosListFragmentArgs>()

    private val args by lazy { navArgs.args ?: PhotoListArgs.default }

    private val listAdapter = MediaItemListAdapter()

    private val listLayoutManager by lazy {
        createGridLayoutManager(requireContext(), NUMBER_OF_COLUMNS) {
            buildSpanSizeLookup { position ->
                val item = listAdapter.getItem(position)
                if (item is MediaItemListItem) 1 else NUMBER_OF_COLUMNS
            }
        }
    }

    private val listInfiniteScrollListener by lazy {
        BidirectionalInfiniteScrollListener(listLayoutManager)
    }

    override fun setupViews() = withBinding {
        toolbar.onFilterClicked = {
            navigate(PhotosListFragmentDirections.toFilterFragment())
        }

        toolbar.onDebugClicked = {
            navigate(PhotosListFragmentDirections.toDebugScreen())
        }

        mediaTypeFilter.onFilterClicked { viewModel.changeMediaType(it) }

        viewAuthError.onRetry = {
            authViewModel.showAuthDialogIfNeeded(this@PhotosListFragment)
        }

        viewUnauthenticated.onRetry = {
            authViewModel.showAuthDialogIfNeeded(this@PhotosListFragment)
        }

        viewError.onRetry = {
            viewModel.init(args.selectedDate)
        }

        setupRecyclerView()
    }

    override fun observeViewModel() {
        authViewModel.init(requireContext())
        authViewModel.onStateChange(owner, ::renderAuthState)

        viewModel.init(args.selectedDate)
        viewModel.onStateChange(owner, ::renderState)
        viewModel.onStateChange(owner, { it.groupedItems }, ::renderListOnChange)
    }

    private fun renderState(state: PhotosListState) {
        e {"rendering state: $state"}
        with(listInfiniteScrollListener) {
            canLoadAny = state.finishedInitialLoad && state.userHasScrolled
            isLoadingTop = state.loadingAfter
            isLoadingBottom = state.loadingBefore
        }

        withBinding {
            mediaTypeFilter.setSelected(state.mediaFilter)

            viewError.visibleOrGone = state.hasError
            viewError.setErrorText(state.errorText)
        }
    }

    private fun renderAuthState(state: PhotosAuthState) {
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

    private fun renderListOnChange(state: PhotosListState) {
        listAdapter.updateGroupedMediaItems(state.groupedItems) {
            if (!state.finishedInitialLoad && !state.userHasScrolled) {
                scrollToCurrentDay(state.targetDate)
            }
        }
    }

    private fun setupRecyclerView() = with(binding.recyclerView) {
        layoutManager = listLayoutManager
        adapter = listAdapter

        with(listInfiniteScrollListener) {
            onLoadMoreTop { viewModel.loadMoreAfter() }
            onLoadMoreBottom { viewModel.loadMoreBefore() }
            addOnScrollListener(this)
        }

        addOnScrollStateDragging { viewModel.setUserScrolled() }
    }

    private fun scrollToCurrentDay(targetDate: LocalDate) {
        val position = listAdapter.getPositionOfTargetDate(targetDate) ?: return

        with(binding.recyclerView) {
            stopScroll()
            layoutManager?.cast<GridLayoutManager>()
                ?.scrollToPositionWithOffset(position, 0)
        }
    }
}
