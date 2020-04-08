package com.worldturtlemedia.playground.photos.googlephotos.ui.list

import android.os.Parcelable
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        e { "Rendering state:\nState: $state" }
        withBinding {
            mediaTypeFilter.setSelected(state.mediaFilter)

            viewError.visibleOrGone = state.hasError
            viewError.setErrorText(state.errorText)
        }
    }

    private fun renderAuthState(state: PhotosAuthState) {
        e { "Rendering auth state:\nAuthState: $state" }
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
        val start = System.currentTimeMillis()

        listAdapter.updateGroupedMediaItems(state.groupedItems) {
            val duration = (System.currentTimeMillis() - start) / 1000.0
            e { "Took $duration seconds to render ${state.groupedItems.flatMap { it.second }.size} items" }

            if (!state.finishedInitialLoad && !state.userHasScrolled) {
                scrollToCurrentDay(state.targetDate)
            }
        }
    }

    private fun setupRecyclerView() = with(binding.recyclerView) {
        layoutManager = createLayoutManager()
        adapter = listAdapter

        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    viewModel.setUserScrolled()
                }
            }
        })
    }

    private fun createLayoutManager() =
        createGridLayoutManager(requireContext(), NUMBER_OF_COLUMNS) {
            buildSpanSizeLookup { position ->
                val item = listAdapter.getItem(position)
                if (item is MediaItemListItem) 1 else NUMBER_OF_COLUMNS
            }
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