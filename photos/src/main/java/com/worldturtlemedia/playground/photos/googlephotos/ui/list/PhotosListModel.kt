package com.worldturtlemedia.playground.photos.googlephotos.ui.list

import androidx.lifecycle.viewModelScope
import com.github.ajalt.timberkt.e
import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.common.ktx.cast
import com.worldturtlemedia.playground.common.ktx.ensureKey
import com.worldturtlemedia.playground.common.ktx.merge
import com.worldturtlemedia.playground.common.ktx.safeCollect
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthRepoFactory
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import com.worldturtlemedia.playground.photos.googlephotos.data.ApiResult
import com.worldturtlemedia.playground.photos.googlephotos.data.asApiError
import com.worldturtlemedia.playground.photos.googlephotos.data.library.LibraryRepository
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.MediaFilter
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import org.joda.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosListModel : StateViewModel<PhotosListState>(PhotosListState()) {

    override val isDebugMode: Boolean = true

    private val authRepo = GoogleAuthRepoFactory.instance

    private val libraryRepo = LibraryRepository.memoryInstance

    private var firstLoadJob: Job? = null

    init {
        viewModelScope.launch {
            authRepo.state.collect { state ->
                firstLoadJob?.let { job ->
                    if (state is GoogleAuthState.Unauthenticated) {
                        e { "Cancelling" }
                        job.cancelChildren()
                    }
                }
            }
        }
    }

    fun init(date: LocalDate) {
        if (currentState.initialized && currentState.status !is ApiResult.Fail) return

        setState { copy(initialized = true, targetDate = date) }

        firstLoadJob?.cancelChildren()
        firstLoadJob = viewModelScope.launch(Dispatchers.IO) {
            e { "Inside firstLoadJob.launch: active -> $isActive" }
            authRepo.state.collect { state ->
                if (state !is GoogleAuthState.Authenticated) return@collect

                // TESTING
                libraryRepo.fetchMediaForDate(date)
                    .catch { exception ->
                        if (exception is CancellationException) return@catch
                        setState { copy(status = exception.asApiError()) }
                    }
                    .safeCollect { result ->
                        val newItems = if (result is ApiResult.Success) result.data else emptyList()

                        setState {
                            copy(
                                items = items.merge(newItems),
                                status = result
                            )
                        }
                    }

                e { "Finished loading initial media" }
                setState {
                    copy(finishedInitialLoad = true)
                }

                firstLoadJob?.cancel()
            }
        }
    }

    fun changeMediaType(type: MediaFilter) = setState {
        copy(mediaFilter = type)
    }

    fun setUserScrolled() = setState {
        if (userHasScrolled) null
        else copy(userHasScrolled = true)
    }
}

data class PhotosListState(
    val initialized: Boolean = false,
    val targetDate: LocalDate = LocalDate.now(),
    val mediaFilter: MediaFilter = MediaFilter.All,
    val items: List<MediaItem> = emptyList(),
    val finishedInitialLoad: Boolean = false,
    val status: ApiResult<List<MediaItem>>? = null,
    val userHasScrolled: Boolean = false
) : State {

    val hasError: Boolean
        get() = status is ApiResult.Fail

    val errorText: String?
        get() = status?.cast<ApiResult.Fail>()?.error?.message

    val groupedItems: List<Pair<LocalDate, List<MediaItem>>>
        get() = items
            .groupBy { item -> item.creationTime.toLocalDate() }
            .ensureKey(targetDate, emptyList())
            .toList()
            .sortedByDescending { (date, _) -> date }

    override fun toString(): String {
        val statustext =
            if (status is ApiResult.Success) "Success(items=${status.data.size})" else status.toString()
        return "PhotoListState(init=$initialized,targetDate=$targetDate,finishedInitialLoad=$finishedInitialLoad,userHasScrolled=$userHasScrolled,status=$statustext"
    }
}