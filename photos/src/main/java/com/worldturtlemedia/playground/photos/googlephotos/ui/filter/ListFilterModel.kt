package com.worldturtlemedia.playground.photos.googlephotos.ui.filter

import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.common.base.ui.viewmodel.launchIO
import com.worldturtlemedia.playground.common.core.SingleEvent
import com.worldturtlemedia.playground.photos.googlephotos.data.AlbumsRepository
import com.worldturtlemedia.playground.photos.googlephotos.data.PhotosResult
import com.worldturtlemedia.playground.photos.googlephotos.model.Album
import kotlinx.coroutines.flow.collect

class ListFilterModel : StateViewModel<ListFilterState>(ListFilterState()) {

    private val albumsRepo = AlbumsRepository.instance


    init {
        fetchAlbums()
    }

    fun loadMoreAlbums() = launchIO {
        albumsRepo.loadMoreAlbums().collect { handleAlbumStatus(it) }
    }

    fun fetchAlbums() = launchIO {
        albumsRepo.fetchAlbums().collect { handleAlbumStatus(it) }
    }

    fun clearFilters() = setState { copy(filters = emptyList()) }

    fun applyFilters() = setState {
        copy(event = SingleEvent(ListFilterEvent.Apply(filters)))
    }

    fun close() = setState { copy(event = SingleEvent(ListFilterEvent.Close)) }

    private fun handleAlbumStatus(status: PhotosResult<List<Album>>) {
        setState {
            copy(
                loadingAlbums = status is PhotosResult.Loading,
                albums = if (status is PhotosResult.Success) status.result else albums,
                albumStatus = SingleEvent(status)
            )
        }
    }
}

data class ListFilterState(
    val loadingAlbums: Boolean = false,
    val albums: List<Album> = emptyList(),
    val albumStatus: SingleEvent<PhotosResult<List<Album>>>? = null,
    val filters: List<String> = emptyList(),
    val event: SingleEvent<ListFilterEvent>? = null
) : State

sealed class ListFilterEvent {
    object Close : ListFilterEvent()
    data class Apply(val filters: List<String>) : ListFilterEvent()
}