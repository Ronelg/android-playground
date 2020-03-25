package com.worldturtlemedia.playground.photos.googlephotos.ui.filter

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.worldturtlemedia.playground.common.base.ui.BaseFragment
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.common.ktx.cast
import com.worldturtlemedia.playground.common.ktx.onClick
import com.worldturtlemedia.playground.common.ktx.visibleOrGone
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import com.worldturtlemedia.playground.photos.auth.ui.PhotosAuthModel
import com.worldturtlemedia.playground.photos.databinding.ListFilterFragmentBinding
import com.worldturtlemedia.playground.photos.databinding.ListFilterFragmentBinding.bind
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.list_filter_fragment.*
import kotlinx.coroutines.launch

class ListFilterFragment : BaseFragment<ListFilterFragmentBinding>(R.layout.list_filter_fragment) {

    private val authViewModel: PhotosAuthModel by activityViewModels()

    private val viewModel: ListFilterModel by viewModels()

    override val binding: ListFilterFragmentBinding by viewBinding { bind(it) }

    private val listAdapter by lazy { GroupAdapter<GroupieViewHolder>() }

    override fun setupViews() = withBinding {
        txtDateRangeClear.onClick { viewModel.setDateRange(null) }
        btnChooseDateRange.onClick {
            lifecycleScope.launch { viewModel.showDateRangeDialog(childFragmentManager) }
        }

        txtCategoryClear.onClick { viewModel.clearCategoryFilters() }

        btnClose.onClick { viewModel.close() }
        btnClearAll.onClick { viewModel.clearAllFilters() }
        btnApply.onClick { viewModel.applyFilters() }

        viewAuthUser.setOnDisconnect { authViewModel.signOut() }

        setupRecyclerView()
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
            binding.txtDateRange.text = state.dateFilter?.toString() ?: ""

            listAdapter.update(createCategoryFilterItems(state.categoryFilters))

            with(binding.btnApply) {
                val size = state.filterCount

                isEnabled = size > 0
                text =
                    if (size <= 0) getString(R.string.no_filters_selected)
                    else resources.getQuantityString(R.plurals.apply_filter, size, size)
            }

            state.event?.consume(::handleModelEvent)
        }
    }

    private fun setupRecyclerView() {
        with(categoryList) {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = listAdapter

            listAdapter.setOnItemClickListener { item, _ ->
                val filterItem = item.cast<CategoryFilterItem>() ?: return@setOnItemClickListener

                viewModel.toggleCategoryFilter(filterItem.category)
            }
        }
    }

    private fun handleModelEvent(event: ListFilterEvent) {
        when (event) {
            is ListFilterEvent.Close -> findNavController().popBackStack()
            is ListFilterEvent.Apply -> findNavController().popBackStack()
        }
    }
}