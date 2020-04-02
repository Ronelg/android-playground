package com.worldturtlemedia.playground.photos.googlephotos.ui.list

import android.os.Parcelable
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
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

        setupRecyclerView()
    }

    override fun observeViewModel() {
        authViewModel.init(requireContext())
        viewModel.setTargetDate(args.selectedDate)

        viewModel.state.observe(owner) { state ->
            binding.mediaTypeFilter.setSelected(state.mediaFilter)
        }

        viewModel.onStateChange(owner, { it.groupedItems }) { state ->
            val start = System.currentTimeMillis()

            listAdapter.updateGroupedMediaItems(state.groupedItems) {
                val duration = (System.currentTimeMillis() - start) / 1000.0
                e { "Took $duration seconds to render ${state.groupedItems.flatMap { it.second }.size} items" }

                if (!state.finishedInitialLoad) {
                    val position = listAdapter.getPositionOfTargetDate(state.targetDate)
                    if (position != null) {
                        e { "Scrolling to $position" }
                        binding.recyclerView.stopScroll()
                        binding.recyclerView.layoutManager?.cast<GridLayoutManager>()
                            ?.scrollToPositionWithOffset(position, 0)
                    }
                }
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
        createGridLayoutManager(requireContext(), NUMBER_OF_COLUMNS) {
            buildSpanSizeLookup { position ->
                val item = listAdapter.getItem(position)
                if (item is MediaItemListItem) 1 else NUMBER_OF_COLUMNS
            }
        }
}