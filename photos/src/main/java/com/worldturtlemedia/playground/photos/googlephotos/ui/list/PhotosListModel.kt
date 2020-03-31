package com.worldturtlemedia.playground.photos.googlephotos.ui.list

import androidx.lifecycle.viewModelScope
import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.common.ktx.merge
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthRepoFactory
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import com.worldturtlemedia.playground.photos.googlephotos.data.ApiResult
import com.worldturtlemedia.playground.photos.googlephotos.data.library.LibraryRepository
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.MediaFilter
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosListModel : StateViewModel<PhotosListState>(PhotosListState()) {

    companion object {

        const val MONTH_YEAR_PATTERN = "MMMM d, Y"
    }

    private val authRepo = GoogleAuthRepoFactory.instance

    private val libraryRepo = LibraryRepository.memoryInstance

    init {
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.state.distinctUntilChanged().collect { state ->
                if (state !is GoogleAuthState.Authenticated) return@collect

                libraryRepo.debugFetchLibraryItems().collect { result ->
                    if (result is ApiResult.Success) {
                        setState { copy(items = items.merge(result.result)) }
                    }
                }
            }
        }
    }

    fun changeMediaType(type: MediaFilter) = setState {
        copy(mediaFilter = type)
    }
}

data class PhotosListState(
    val mediaFilter: MediaFilter = MediaFilter.All,
    val items: List<MediaItem> = emptyList()
) : State

fun List<MediaItem>.groupedByDate(): List<Pair<String, List<MediaItem>>> {
    return groupBy { item ->
        item.creationTime.toString(PhotosListModel.MONTH_YEAR_PATTERN)
    }.toList()
}