package com.worldturtlemedia.playground.photos.googlephotos.ui.list

import androidx.lifecycle.viewModelScope
import com.github.ajalt.timberkt.e
import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.common.ktx.ensureKey
import com.worldturtlemedia.playground.common.ktx.merge
import com.worldturtlemedia.playground.common.ktx.safeCollect
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthRepoFactory
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import com.worldturtlemedia.playground.photos.googlephotos.data.ApiResult
import com.worldturtlemedia.playground.photos.googlephotos.data.library.LibraryRepository
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.MediaFilter
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.joda.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosListModel : StateViewModel<PhotosListState>(PhotosListState()) {

    private val authRepo = GoogleAuthRepoFactory.instance

    private val libraryRepo = LibraryRepository.memoryInstance


    fun setTargetDate(date: LocalDate) {
        setState { copy(targetDate = date) }

        viewModelScope.launch(Dispatchers.IO) {
            authRepo.state.distinctUntilChanged().safeCollect { state ->
                if (state !is GoogleAuthState.Authenticated) return@safeCollect

                // TESTING
                libraryRepo.fetchMediaForDate(date).safeCollect { result ->
                    if (result is ApiResult.Success) {
                        setState {
                            copy(items = items.merge(result.data))
                        }
                    }
                }

                setState { copy(finishedInitialLoad = true) }
            }

            e { "Finished with authRepo.state collect" }
        }
    }

    fun changeMediaType(type: MediaFilter) = setState {
        copy(mediaFilter = type)
    }

}

data class PhotosListState(
    val targetDate: LocalDate = LocalDate.now(),
    val mediaFilter: MediaFilter = MediaFilter.All,
    val items: List<MediaItem> = emptyList(),
    val finishedInitialLoad: Boolean = false
) : State {

    val groupedItems: List<Pair<LocalDate, List<MediaItem>>>
        get() = items
            .groupBy { item -> item.creationTime.toLocalDate() }
            .ensureKey(targetDate, emptyList())
            .toList()
            .sortedByDescending { (date, _) -> date }
}