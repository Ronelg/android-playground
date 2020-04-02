package com.worldturtlemedia.playground.photos.googlephotos.ui.calendar

import androidx.fragment.app.viewModels
import com.worldturtlemedia.playground.common.base.ui.BaseFragment
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.common.ktx.cast
import com.worldturtlemedia.playground.common.ktx.createGridLayoutManager
import com.worldturtlemedia.playground.common.ktx.navigate
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.CalendarFragmentBinding
import com.worldturtlemedia.playground.photos.databinding.CalendarFragmentBinding.bind
import com.worldturtlemedia.playground.photos.googlephotos.ui.Constants
import com.worldturtlemedia.playground.photos.googlephotos.ui.list.PhotoListArgs
import com.worldturtlemedia.playground.photos.googlephotos.ui.list.PhotosListFragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class CalendarFragment : BaseFragment<CalendarFragmentBinding>(R.layout.calendar_fragment) {

    override val binding: CalendarFragmentBinding by viewBinding { bind(it) }

    private val viewModel: CalendarModel by viewModels()

    private val listAdapter by lazy {
        GroupAdapter<GroupieViewHolder>().apply {
            setOnItemClickListener { item, _ ->
                val calendarItem = item.cast<CalendarListItem>() ?: return@setOnItemClickListener
                viewModel.selectMediaForDate(calendarItem.date)
            }
        }
    }

    override fun setupViews() {
        setupRecyclerView()
    }

    override fun observeViewModel() {
        viewModel.observeProperty(owner, { it.items }) { list ->
            val (today, _) = viewModel.calendarDates

            listAdapter.updateAsync(createCalendarListItems(list, today))
        }

        viewModel.observe(owner) { state ->
            state.scrollToBottom?.consume { shouldScroll ->
                if (shouldScroll) {
                    binding.recyclerView.scrollToPosition(listAdapter.itemCount - 1)
                }
            }

            state.navEvent?.consume { targetDate ->
                val directions =
                    CalendarFragmentDirections.toSelectMediaItem(PhotoListArgs(targetDate))
                navigate(directions)
            }
        }
    }

    private fun setupRecyclerView() = with(binding.recyclerView) {
        adapter = listAdapter
        layoutManager = createGridLayoutManager(context, Constants.NUMBER_OF_COLUMNS) {

        }
    }
}