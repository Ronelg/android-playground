package com.worldturtlemedia.playground.photos.googlephotos.data.library

import com.github.ajalt.timberkt.e
import com.worldturtlemedia.playground.photos.googlephotos.data.ApiResult
import com.worldturtlemedia.playground.photos.googlephotos.data.PhotosClient
import com.worldturtlemedia.playground.photos.googlephotos.data.PhotosClientFactory
import com.worldturtlemedia.playground.photos.googlephotos.data.emitSuccess
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.DateFilter
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.buildFilters
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.toModels
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.joda.time.LocalDate
import kotlin.coroutines.coroutineContext

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LibraryRepository(
    clientFactory: PhotosClientFactory,
    private val mediaItemCache: MediaItemCache
) : PhotosClient by clientFactory {

    companion object Factory {
        val memoryInstance by lazy {
            LibraryRepository(PhotosClientFactory.instance, MemoryMediaItemCache.instance)
        }

        val dbInstance by lazy {
            LibraryRepository(PhotosClientFactory.instance, DatabaseMediaItemCache.instance)
        }
    }

    suspend fun fetchMediaForDate(
        targetDate: LocalDate
    ) = flow<ApiResult<List<MediaItem>>> {
        e { "Fetching for $targetDate" }
        val filter = buildFilters(DateFilter.YearMonthDay(targetDate))

        val start = System.currentTimeMillis()
        safeApiCall { client ->
            val response = client.searchMediaItems(filter).iteratePages()
            e { "Got response in ${(System.currentTimeMillis() - start) / 1000.0}s" }

            response.mapIndexedNotNull { index, page ->
                val items = page.values.toModels()
                e { "Got ${items.size} from page ${index + 1}" }
                emitSuccess(items)
            }
        }

        e { "Got all items in ${(System.currentTimeMillis() - start) / 1000.0}s" }

        val flow1 = fetchItemsBefore(targetDate)
        val flow2 = fetchItemsAfter(targetDate)
        val t = flowOf(flow1, flow2).flattenMerge()
        emitAll(t)
//        emitAll(fetchItemsBefore(targetDate))
    }.flowOn(Dispatchers.IO)

    suspend fun fetchItemsBefore(
        targetDate: LocalDate,
        minimumItems: Int = 20,
        monthsBefore: Int = 3,
        maxCalls: Int = 4, // 12 Months
        callCount: Int = 0,
        previousItemCount: Int = 0
    ): Flow<ApiResult<List<MediaItem>>> = flow {
        e { "Starting before fetch! Call #${callCount + 1} with $previousItemCount existing items" }
        val rangeStart = targetDate.minusMonths(monthsBefore)
        val rangeEnd = targetDate.minusDays(1)
        val filter = buildFilters(DateFilter.range(rangeStart, rangeEnd))

        val result = safeApiCall { client ->
            val pages = client.searchMediaItems(filter).iteratePages()
            val allItems = mutableListOf<MediaItem>()
            for (page in pages) {
                coroutineContext.ensureActive()

                val items = page.values.toModels()
                allItems.addAll(items)
                emit(ApiResult.Success(items))

                if ((previousItemCount + allItems.size) >= minimumItems) {
                    e { "Met the minimum! time to break" }
                    break
                }
            }

            allItems
        }

        if (result !is ApiResult.Success) return@flow emit(result)

        val itemsCount = previousItemCount + result.data.size
        if (callCount >= maxCalls && itemsCount < minimumItems) {
            e { "Too many retries with not enough items... stopping" }
        } else if (itemsCount >= minimumItems) {
            e { "Met the minimum size! ${result.data.size} with ${callCount + 1} calls" }
        } else {
            e { "Did not meet minimum items! Attempting to recurse..." }
            fetchItemsBefore(
                targetDate = rangeStart,
                minimumItems = minimumItems,
                monthsBefore = monthsBefore,
                maxCalls = maxCalls,
                callCount = callCount + 1,
                previousItemCount = itemsCount
            ).let { emitAll(it) }
        }
    }

    suspend fun fetchItemsAfter(
        targetDate: LocalDate,
        minimumItems: Int = 20,
        daysAfter: Int = 15
    ) = flow<ApiResult<List<MediaItem>>> {
        e { "Starting after fetch!" }

        val allItems = mutableListOf<MediaItem>()
        for (day in 1 until daysAfter) {
            coroutineContext.ensureActive()

            val date = targetDate.plusDays(day)
            val filter = buildFilters(DateFilter.YearMonthDay(date))

            if (date.isEqual(LocalDate.now())) {
                e { "Target $date is in the future, I'm no psychic, cancelling" }
                return@flow
            }

            e { "Starting day $date" }

            val result = safeApiCall { client ->
                val pages = client.searchMediaItems(filter).iteratePages()
                var pageI = 1
                for (page in pages) {
                    coroutineContext.ensureActive()

                    val items = page.values.toModels()
                    e { "Found ${items.size} items for page#$pageI" }
                    allItems.addAll(items)
                    emit(ApiResult.Success(items))

                    if (allItems.size >= minimumItems) {
                        e { "Met the minimum! time to break" }
                        break
                    }

                    pageI++
                }

                allItems
            }

            if (result !is ApiResult.Success) {
                emit(result)
                break
            }

            if (allItems.size >= minimumItems) {
                e { "Met the minimum! time to break" }
                break
            }
        }

        if (allItems.size < minimumItems) e { "Did not meet minimum items! ${allItems.size}" }
        else e { "Met the minimum size! ${allItems.size}" }
    }


    suspend fun fetchItems(
        targetDate: LocalDate,
        daysBefore: Int = 5,
        daysAfter: Int = 5
    ) = flow<ApiResult<List<MediaItem>>> {
        val rangeStart = targetDate.minusDays(daysBefore)
        val rangeEnd =
            if (targetDate.isEqual(LocalDate.now())) targetDate
            else targetDate.plusDays(daysAfter)

        val filters = buildFilters(DateFilter.range(rangeStart, rangeEnd))

        safeApiCall { client ->
            val pages = client.searchMediaItems(filters).iteratePages()
            var pageCount = 1
            var itemCount = 0
            for (page in pages) {
                if (!coroutineContext.isActive) return@safeApiCall

                val items = page.values.toModels()

                pageCount++
                itemCount += items.size

                e { "Loaded page#$pageCount with ${items.size} items" }

                emit(ApiResult.Success(items))

            }

            e { "Finished with $pageCount pages and $itemCount items" }
        }
    }.flowOn(Dispatchers.IO)
}
