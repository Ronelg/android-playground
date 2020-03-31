package com.worldturtlemedia.playground.photos.googlephotos.data.library

import com.github.ajalt.timberkt.e
import com.worldturtlemedia.playground.photos.googlephotos.data.ApiResult
import com.worldturtlemedia.playground.photos.googlephotos.data.PhotosClient
import com.worldturtlemedia.playground.photos.googlephotos.data.PhotosClientFactory
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.toModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
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

    suspend fun debugFetchLibraryItems(minItems: Int = 100) = flow<ApiResult<List<MediaItem>>> {
        val start = System.currentTimeMillis()
        safeApiCall { client ->
            val response = client.listMediaItems()
            var itemCount = 0
            var pageCount = 1
            var previousMillis: Long = 0
            for (page in response.iteratePages()) {
                val items = page.values.toModels().also { list ->
                    itemCount += list.size

                    val time = (System.currentTimeMillis() - start) // 1500
                    val biz = time - previousMillis
                    previousMillis = time

                    val foo = biz / 1000.0
                    e { "Loaded page #$pageCount with ${list.size} items in $foo seconds" }
                }

                emit(ApiResult.Success(items))

                if (itemCount > minItems) {
                    val duration = (System.currentTimeMillis() - start) / 1000.0
                    e { "Finished loading $pageCount pages with $itemCount items in $duration seconds" }
                    break
                }

                pageCount++
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun test(): ApiResult<List<MediaItem>> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val boop = safeApiCall { client ->
            e { "Fetching items from the API" }
//            val response = client.listMediaItems().iterateFixedSizeCollections()

//            val list = mutableListOf<MediaItem>()
//            for(item in response.page.response.mediaItemsList) {
//                e { "Mapping item #${list.size+1}" }
//                val mapped = item.toModel() ?: continue
//                list.add(mapped)
//            }
//
//            val duration = System.currentTimeMillis() - start
//            e { "Took ${duration / 1000} seconds to fetch ${list.size} items" }
//
//            list
            var count = 1
            val test = client.listMediaItems().iteratePages().flatMap { page ->
                e { "Getting items from page: $count" }
                page.values
                    ?.also { items ->
                        e { "Got ${items.count()} items from page $count" }
                        count++
                    } ?: emptyList()
            }

            test.toModels().also { list ->
                val duration = System.currentTimeMillis() - start
                e { "Took ${duration / 1000} seconds to fetch ${list.size} items" }
            }
        }

        boop
    }
}
