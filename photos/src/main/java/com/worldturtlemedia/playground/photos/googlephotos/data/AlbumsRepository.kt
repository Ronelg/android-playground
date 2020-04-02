package com.worldturtlemedia.playground.photos.googlephotos.data

import com.github.ajalt.timberkt.e
import com.github.ajalt.timberkt.i
import com.google.api.gax.rpc.ApiException
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient
import com.worldturtlemedia.playground.common.ktx.emitAndLog
import com.worldturtlemedia.playground.photos.db.PhotosDatabase
import com.worldturtlemedia.playground.photos.googlephotos.data.db.album.AlbumDao
import com.worldturtlemedia.playground.photos.googlephotos.data.db.album.mapToModel
import com.worldturtlemedia.playground.photos.googlephotos.data.db.album.toEntities
import com.worldturtlemedia.playground.photos.googlephotos.model.Album
import com.worldturtlemedia.playground.photos.googlephotos.model.toModelList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsRepository(
    delegate: PhotosClientFactory,
    private val albumDao: AlbumDao
) : PhotosClient by delegate {

    companion object Factory {
        val instance by lazy {
            AlbumsRepository(PhotosClientFactory.instance, PhotosDatabase.getInstance().albumDao())
        }
    }

    private var currentPage: InternalPhotosLibraryClient.ListAlbumsPage? = null

    val hasMorePages: Boolean
        get() = currentPage?.hasNextPage() ?: true

    suspend fun fetchAlbums(force: Boolean = false): Flow<ApiResult<List<Album>>> = flow {
        try {
            // Tell the observer that we're loading!
            emit(ApiResult.Loading)

            // Load any existing albums from the database first
            val existingAlbums = loadAlbumsFromDatabase()
            if (existingAlbums.isNotEmpty()) {
                emit(ApiResult.Success(existingAlbums))
            }

            // If we've already loaded the first page, and there is some data, return early
            if (!force && currentPage != null && existingAlbums.isNotEmpty()) return@flow

            emit(ApiResult.Loading)

            // Fetch new albums from the API
            when (val remoteResult = loadInitialAlbums()) {
                is ApiResult.Fail -> emit(remoteResult)
                is ApiResult.Success -> {
                    // Store the new albums in the database
                    storeAPIResultInDatabase(remoteResult.data)

                    // Return the album values from the database
                    emit(ApiResult.Success(loadAlbumsFromDatabase()))
                }
            }
        } catch (error: Throwable) {
            e(error) { "Unable to fetch albums" }

            val type =
                if (error is ApiException) ApiError.RequestFail
                else ApiError.Error(error)

            emit(ApiResult.Fail(type))
        }
    }

    suspend fun loadMoreAlbums(): Flow<ApiResult<List<Album>>> = flow {
        // If there is no next page to load, return early
        val nextPage = currentPage?.nextPage
        if (nextPage == null || !hasMorePages) return@flow

        val results = try {
            // Fetch the next page of Albums from the API
            nextPage.iterateAll().toModelList().also { list ->
                // Store these new albums in the database
                storeAPIResultInDatabase(list)
            }

            // Use the values from the database
            loadAlbumsFromDatabase()
        } catch (error: Throwable) {
            e(error) { "Unable to fetch the next page!" }
            emit(ApiResult.Fail(ApiError.RequestFail))

            return@flow
        }

        emitSuccess(results)
    }

    private suspend fun loadAlbumsFromDatabase(): List<Album> {
        return albumDao.allAlbums().mapToModel()
    }

    private suspend fun loadInitialAlbums(): ApiResult<List<Album>> =
        safeApiCall { client ->
            val albumsResponse = client.listAlbums()
            currentPage = albumsResponse.page

            albumsResponse.iterateAll().toModelList()
        }

    private suspend fun storeAPIResultInDatabase(data: List<Album>) {
        val entities = data.toEntities()
        albumDao.insert(entities)
    }

    /**
     * This is just for testing out the API, not production ready code (:
     */
    suspend fun debugFetchAllAlbums(): Flow<ApiResult<List<Album>>> = flow {
        emitAndLog(ApiResult.Loading)
        val firstLoad = safeApiCall { it.listAlbums() }

        if (firstLoad !is ApiResult.Success) {
            emitAndLog(ApiResult.Fail(ApiError.RequestFail))
            return@flow
        }

        val list = mutableListOf<Album>()
        var page = firstLoad.data.page
        var count = 1
        do {
            i { "Loading page $count" }
            val result = page.iterateAll().toModelList()
            i { "Loaded page $count with ${result.size} items" }
            list.addAll(result)
            page = page.nextPage
            count++
        } while (page != null)

        i { "Loaded ${list.size} albums from $count pages." }
        emit(ApiResult.Success(list))
    }
}