package com.worldturtlemedia.playground.photos.googlephotos.ui.list

import androidx.lifecycle.viewModelScope
import com.github.ajalt.timberkt.e
import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.common.base.ui.viewmodel.launchIO
import com.worldturtlemedia.playground.common.core.SingleEvent
import com.worldturtlemedia.playground.common.ktx.*
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthRepoFactory
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import com.worldturtlemedia.playground.photos.googlephotos.data.ApiError
import com.worldturtlemedia.playground.photos.googlephotos.data.ApiResult
import com.worldturtlemedia.playground.photos.googlephotos.data.asApiError
import com.worldturtlemedia.playground.photos.googlephotos.data.dataOrNull
import com.worldturtlemedia.playground.photos.googlephotos.data.library.LibraryRepository
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.MediaFilter
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.createdDate
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.isPhoto
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.isVideo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.joda.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosListModel : StateViewModel<PhotosListState>(PhotosListState()) {

    override val isDebugMode: Boolean = true

    private val authRepo = GoogleAuthRepoFactory.instance

    private val libraryRepo = LibraryRepository.dbInstance

    private var firstLoadJob: Job? = null
    private var loadAfterJob: Job? = null
    private var loadBeforeJob: Job? = null

    init {
        viewModelScope.launch {
            authRepo.state.collect { state ->
                if (state is GoogleAuthState.Unauthenticated) {
                    firstLoadJob?.cancelChildren()
                }
            }
        }
    }

    fun init(date: LocalDate) {
        if (currentState.initialized && currentState.initialLoadStatus !is ApiResult.Fail) return

        setState { copy(initialized = true, targetDate = date) }

        firstLoadJob?.cancelChildren()
        firstLoadJob = launchIO {
            authRepo.state
                .onCompletion {
                    e { "Finished loading initial media" }
                    setState { copy(finishedInitialLoad = true) }
                }
                .collect { state ->
                    if (state !is GoogleAuthState.Authenticated) return@collect

                    // TESTING
                    e { "Loading initial for $date" }
                    libraryRepo.fetchMediaForDate(date)
                        .catch { exception ->
                            if (exception is CancellationException) return@catch
                            setState { copy(initialLoadStatus = exception.asApiError()) }
                        }
                        .safeCollect { result ->
                            val newItems = result.dataOrNull() ?: emptyList()

                            if (result is ApiResult.Fail) {
                                if (result.error is ApiError.Unauthenticated) {
                                    e { "We are not authenticated..." }
                                    authRepo.signOut()
                                }
                            }

                            setState {
                                copy(
                                    items = items.mergeNewResults(newItems),
                                    initialLoadStatus = result
                                )
                            }
                        }

                    firstLoadJob?.cancel()
                }
        }
    }

    fun changeMediaType(type: MediaFilter) = setState {
        if (mediaFilter.peek() == type) null
        else copy(mediaFilter = SingleEvent(type))
    }

    fun setUserScrolled() = setState {
        if (userHasScrolled) null
        else copy(userHasScrolled = true)
    }

    fun loadMoreAfter() {
        if (loadAfterJob != null && loadAfterJob?.isActive == true) return

        val firstDate = currentState.items.firstOrNull()?.createdDate ?: return
        launchIO {
            e { "Loading MORE AFTER $firstDate" }
            libraryRepo.fetchItemsAfter(firstDate)
                .handleResult { isLoading -> copy(loadingAfter = isLoading) }

            e { "Done loading AFTER" }
            loadAfterJob = null
        }
    }

    fun loadMoreBefore() {
        if (loadBeforeJob != null && loadBeforeJob?.isActive == true) return

        val lastDate = currentState.items.lastOrNull()?.createdDate ?: return
        launchIO {
            e { "Loading MORE BEFORE $lastDate" }
            libraryRepo.fetchItemsBefore(lastDate)
                .handleResult { isLoading -> copy(loadingBefore = isLoading) }

            e { "Done loading BEFORE" }
            loadBeforeJob = null
        }
    }

    // TODO: Have some type of error handling on here to inform the user we can't load more
    private suspend fun Flow<ApiResult<List<MediaItem>>>.handleResult(
        updateLoading: PhotosListState.(Boolean) -> PhotosListState
    ) = onStart { setState { updateLoading(this, true) } }
        .onCompletion { setState { updateLoading(this, false) } }
        .mapNotNull { result -> result.dataOrNull() }
        .safeCollect { newItems ->
            setState {
                copy(items = items.mergeNewResults(newItems))
            }
        }

    private fun List<MediaItem>.mergeNewResults(list: List<MediaItem>) =
        merge(list).distinctBy { it.id }.sortedByDescending { item -> item.creationTime }
}

data class PhotosListState(
    val initialized: Boolean = false,
    val targetDate: LocalDate = LocalDate.now(),
    val mediaFilter: SingleEvent<MediaFilter> = SingleEvent(MediaFilter.All),
    val items: List<MediaItem> = emptyList(),
    val finishedInitialLoad: Boolean = false,
    val initialLoadStatus: ApiResult<List<MediaItem>>? = null,
    val userHasScrolled: Boolean = false,
    val loadingBefore: Boolean = false,
    val loadingAfter: Boolean = false
) : State {

    val hasError: Boolean
        get() = initialLoadStatus is ApiResult.Fail

    val errorText: String?
        get() = initialLoadStatus?.cast<ApiResult.Fail>()?.error?.message

    val groupedItems: List<Pair<LocalDate, List<MediaItem>>>
        get() = items
            .groupBy { item -> item.creationTime.toLocalDate() }
            .ensureKey(targetDate, emptyList())
            .toList()
            .sortedByDescending { (date, _) -> date }

    override fun toString(): String {
        val statustext =
            if (initialLoadStatus is ApiResult.Success) "Success(items=${initialLoadStatus.data.size})" else initialLoadStatus.toString()
        return "PhotoListState(init=$initialized,targetDate=$targetDate,items=${items.size},finishedInitialLoad=$finishedInitialLoad,userHasScrolled=$userHasScrolled,beforeLoad=${loadingBefore},afterLoad=${loadingAfter},status=$statustext"
    }
}
