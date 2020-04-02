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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
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
    }.flowOn(Dispatchers.IO)

    suspend fun fetchItemsBefore(
        targetDate: LocalDate,
        minimumItems: Int = 20,
        daysBefore: Int = 15
    ) = flow<ApiResult<List<MediaItem>>> {
        e { "Starting before fetch!" }
        val rangeStart = targetDate.minusDays(daysBefore)
        val rangeEnd = targetDate.minusDays(1)
        val filter = buildFilters(DateFilter.range(rangeStart, rangeEnd))

        val result = safeApiCall { client ->
            val pages = client.searchMediaItems(filter).iteratePages()
            val allItems = mutableListOf<MediaItem>()
            for (page in pages) {
                val items = page.values.toModels()
                allItems.addAll(items)
                emit(ApiResult.Success(items))

                if (allItems.size >= minimumItems) {
                    e { "Met the minimum! time to break" }
                    break
                }
            }

            allItems
        }

        if (result !is ApiResult.Success) return@flow emit(result)

        if (result.data.size < minimumItems) e { "Did not meet minimum items! ${result.data.size}" }
        else e { "Met the minimum size! ${result.data.size}" }
    }

    suspend fun fetchItemsAfter(
        targetDate: LocalDate,
        minimumItems: Int = 20,
        daysAfter: Int = 15
    ) = flow<ApiResult<List<MediaItem>>> {
        e { "Starting after fetch!" }
        if (targetDate.isEqual(LocalDate.now())) return@flow

        val allItems = mutableListOf<MediaItem>()
        for (day in 1 until daysAfter) {
            val date = targetDate.plusDays(day)
            val filter = buildFilters(DateFilter.YearMonthDay(date))

            e { "Starting day $date" }

            val result = safeApiCall { client ->
                val pages = client.searchMediaItems(filter).iteratePages()
                var pageI = 1
                for (page in pages) {
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
