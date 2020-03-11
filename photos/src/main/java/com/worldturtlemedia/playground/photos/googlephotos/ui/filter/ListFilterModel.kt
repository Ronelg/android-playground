package com.worldturtlemedia.playground.photos.googlephotos.ui.filter

import androidx.lifecycle.viewModelScope
import com.google.photos.types.proto.Album
import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.common.core.SingleEvent
import com.worldturtlemedia.playground.photos.googlephotos.data.GooglePhotosRepo
import com.worldturtlemedia.playground.photos.googlephotos.data.PhotosResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ListFilterModel : StateViewModel<ListFilterState>(ListFilterState()) {

    private val googlePhotosRepo = GooglePhotosRepo.instance


    init {
        viewModelScope.launch(Dispatchers.IO) {
            // TODO: Temporary
            googlePhotosRepo.test().collect { value ->
                if (value is PhotosResult.Success<List<Album>>) {
                    setState { copy(albums = value.result) }
                }
            }
        }
    }

    fun clearFilters() = setState { copy(filters = emptyList()) }

    fun applyFilters() = setState {
        copy(event = SingleEvent(ListFilterEvent.Apply(filters)))
    }

    fun close() = setState { copy(event = SingleEvent(ListFilterEvent.Close)) }
}

data class ListFilterState(
    val filters: List<String> = emptyList(),
    val event: SingleEvent<ListFilterEvent>? = null,
    val albums: List<Album> = emptyList()
) : State

sealed class ListFilterEvent {
    object Close : ListFilterEvent()
    data class Apply(val filters: List<String>) : ListFilterEvent()
}