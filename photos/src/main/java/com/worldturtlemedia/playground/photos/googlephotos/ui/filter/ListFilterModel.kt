package com.worldturtlemedia.playground.photos.googlephotos.ui.filter

import androidx.fragment.app.FragmentManager
import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.common.core.SingleEvent
import com.worldturtlemedia.playground.common.ktx.copy
import com.worldturtlemedia.playground.common.ktx.indexOfOrNull
import com.worldturtlemedia.playground.common.ktx.merge
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.CategoryFilter
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.DateFilter
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.Filter
import org.joda.time.Interval

class ListFilterModel : StateViewModel<ListFilterState>(ListFilterState()) {

    fun clearAllFilters() = setState {
        copy(dateFilter = null, categoryFilters = emptyList())
    }

    suspend fun showDateRangeDialog(fragmentManager: FragmentManager) {
        if (currentState.showingDateRangeDialog) return

        setState { copy(showingDateRangeDialog = true) }

        val rangeInterval = showDateRangePickerDialog(fragmentManager)
        setState {
            copy(
                showingDateRangeDialog = false,
                dateFilter = rangeInterval?.let { DateFilter.Range(it) }
            )
        }
    }

    fun setDateRange(interval: Interval?) = setState {
        copy(
            dateFilter = interval?.let { DateFilter.Range(it) }
        )
    }

    fun toggleCategoryFilter(filter: CategoryFilter) = setState {
        val list = categoryFilters.copy {
            val index = indexOfOrNull(filter)

            if (index == null) add(filter)
            else removeAt(index)
        }

        copy(categoryFilters = list)
    }

    fun clearCategoryFilters() = setState { copy(categoryFilters = emptyList()) }

    fun applyFilters() = setState {
        copy(event = SingleEvent(ListFilterEvent.Apply(filters)))
    }

    fun close() = setState { copy(event = SingleEvent(ListFilterEvent.Close)) }
}

data class ListFilterState(
    val dateFilter: DateFilter.Range? = null,
    val categoryFilters: List<CategoryFilter> = emptyList(),
    val event: SingleEvent<ListFilterEvent>? = null,
    val showingDateRangeDialog: Boolean = false
) : State {

    val filters: List<Filter>
        get() = listOfNotNull<Filter>(dateFilter).merge(categoryFilters)

    val filterCount: Int
        get() = filters.size
}

sealed class ListFilterEvent {
    object Close : ListFilterEvent()
    data class Apply(val filters: List<Filter>) : ListFilterEvent()
}

//private fun handleAlbumStatus(status: PhotosResult<List<Album>>) {
//    setState {
//        copy(
//            loadingAlbums = status is PhotosResult.Loading,
//            albums = if (status is PhotosResult.Success) status.result else albums,
//            albumStatus = SingleEvent(status)
//        )
//    }
//}