package com.worldturtlemedia.playground.photos.googlephotos.ui.filter

import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.common.core.SingleEvent

class ListFilterModel : StateViewModel<ListFilterState>(ListFilterState()) {

    fun clearFilters() = setState { copy(filters = emptyList()) }

    fun applyFilters() = setState {
        copy(event = SingleEvent(ListFilterEvent.Apply(filters)))
    }

    fun close() = setState { copy(event = SingleEvent(ListFilterEvent.Close)) }
}

data class ListFilterState(
    val filters: List<String> = emptyList(),
    val event: SingleEvent<ListFilterEvent>? = null
) : State

sealed class ListFilterEvent {
    object Close : ListFilterEvent()
    data class Apply(val filters: List<String>) : ListFilterEvent()
}