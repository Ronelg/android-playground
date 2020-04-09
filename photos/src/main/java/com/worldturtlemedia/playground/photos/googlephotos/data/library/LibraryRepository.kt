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
import org.joda.time.Days
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
        targetDate: LocalDate,
        force: Boolean = false
    ) = flow<ApiResult<List<MediaItem>>> {
        e { "Fetching for $targetDate" }

        val start = System.currentTimeMillis()
        val cached = mediaItemCache.getByDate(targetDate)

        if (!force && cached.isNotEmpty()) {
            emitSuccess(cached)
        } else {
            val filter = buildFilters(DateFilter.YearMonthDay(targetDate))
            safeApiCall { client ->
                val response = client.searchMediaItems(filter).iteratePages()

                response.mapNotNull { page ->
                    val items = page.values.toModels()
                    emitSuccess(items)
                    mediaItemCache.storeItems(items)
                }
            }
        }

        e { "Got all items in ${(System.currentTimeMillis() - start) / 1000.0}s" }

        emitAll(
            flowOf(
                fetchItemsBefore(targetDate),
                fetchItemsAfter(targetDate)
            ).flattenMerge()
        )
    }.flowOn(Dispatchers.IO)

    suspend fun fetchItemsBefore(
        targetDate: LocalDate,
        minimumItems: Int = 30,
        monthsBefore: Int = 3,
        maxCalls: Int = 4, // 12 Months
        callCount: Int = 0,
        previousItemCount: Int = 0
    ): Flow<ApiResult<List<MediaItem>>> = flow {
        val rangeStart = targetDate.minusMonths(monthsBefore)
        val rangeEnd = targetDate.minusDays(1)

        val cached = mediaItemCache.getAllWithin(rangeStart, rangeEnd)
        emit(ApiResult.Success(cached))
        if (cached.size >= minimumItems) {
            return@flow
        }

        e { "Starting before fetch! Call #${callCount + 1} with $previousItemCount existing items" }
        val filter = buildFilters(DateFilter.range(rangeStart, rangeEnd))
        val result = safeApiCall { client ->
            val pages = client.searchMediaItems(filter).iteratePages()
            val allItems = cached.toMutableList()
            for (page in pages) {
                coroutineContext.ensureActive()

                val items = page.values.toModels()
                allItems.addAll(items)
                mediaItemCache.storeItems(items)
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
        minimumItems: Int = 30,
        daysAfter: Int = 15,
        maxCalls: Int = 12, // 6 months
        callCount: Int = 0,
        previousItemCount: Int = 0
    ): Flow<ApiResult<List<MediaItem>>> = flow {
        e { "Starting after fetch!" }

        val rangeStart = targetDate.plusDays(1)
        if (rangeStart.isAfter(LocalDate.now())) return@flow

        val daysUntilToday = Days.daysBetween(rangeStart, LocalDate.now()).days
        val daysToFetch = if (daysUntilToday < daysAfter) daysUntilToday else daysAfter

        // TODO: Sometimes this is setting the start after the end
        // When it reaches the current day
        val cached = mediaItemCache.getAllWithin(rangeStart, rangeStart.plusDays(daysToFetch))
        emit(ApiResult.Success(cached))

        if (cached.size >= minimumItems) return@flow

        e { "Starting after fetch! Call #${callCount + 1} with $previousItemCount existing items" }

        val allItems = cached.toMutableList()
        for (day in 1 until daysToFetch) {
            coroutineContext.ensureActive()

            val date = targetDate.plusDays(day)
            val filter = buildFilters(DateFilter.YearMonthDay(date))

            e { "Starting day $date" }

            val result = safeApiCall { client ->
                val pages = client.searchMediaItems(filter).iteratePages()
                for (page in pages) {
                    coroutineContext.ensureActive()

                    val items = page.values.toModels()
                    allItems.addAll(items)
                    mediaItemCache.storeItems(items)
                    emit(ApiResult.Success(items))

                    if (allItems.size >= minimumItems) break
                }

                allItems
            }

            if (result !is ApiResult.Success) {
                emit(result)
                return@flow
            }

            if (allItems.size >= minimumItems) {
                e { "Met the minimum! time to break" }
                break
            }
        }

        val itemsCount = previousItemCount + allItems.size
        if (callCount >= maxCalls && itemsCount < minimumItems) {
            e { "Too many retries with not enough items... stopping" }
        } else if (itemsCount >= minimumItems) {
            e { "Met the minimum size! ${allItems.size} with ${callCount + 1} calls" }
        } else {
            e { "Did not meet minimum items! Attempting to recurse..." }
            val newTargetDate = rangeStart.plusDays(daysToFetch)
            if (newTargetDate.isAfter(LocalDate.now())) {
                e { "No more days to look at!" }
                return@flow
            }

            fetchItemsAfter(
                targetDate = newTargetDate,
                minimumItems = minimumItems,
                daysAfter = daysAfter,
                maxCalls = maxCalls,
                callCount = callCount + 1,
                previousItemCount = itemsCount
            ).let { emitAll(it) }
        }
    }
}
