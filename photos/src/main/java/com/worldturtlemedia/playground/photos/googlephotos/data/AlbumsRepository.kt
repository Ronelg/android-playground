package com.worldturtlemedia.playground.photos.googlephotos.data

import com.github.ajalt.timberkt.e
import com.google.api.gax.rpc.ApiException
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient
import com.worldturtlemedia.playground.photos.db.PhotosDatabase
import com.worldturtlemedia.playground.photos.googlephotos.data.db.AlbumDao
import com.worldturtlemedia.playground.photos.googlephotos.data.db.mapToModel
import com.worldturtlemedia.playground.photos.googlephotos.data.db.toEntities
import com.worldturtlemedia.playground.photos.googlephotos.model.Album
import com.worldturtlemedia.playground.photos.googlephotos.model.toModelList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsRepository(
    private val delegate: PhotosClientFactory,
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

    suspend fun fetchAlbums(force: Boolean = false): Flow<PhotosResult<List<Album>>> = flow {
        try {
            // Tell the observer that we're loading!
            emit(PhotosResult.Loading)

            // Load any existing albums from the database first
            val existingAlbums = loadAlbumsFromDatabase()
            if (existingAlbums.isNotEmpty()) {
                emit(PhotosResult.Success(existingAlbums))
            }

            // If we've already loaded the first page, and there is some data, return early
            if (!force && currentPage != null && existingAlbums.isNotEmpty()) return@flow

            emit(PhotosResult.Loading)

            // Fetch new albums from the API
            when (val remoteResult = loadInitialAlbums()) {
                is PhotosResult.Fail -> emit(remoteResult)
                is PhotosResult.Success -> {
                    // Store the new albums in the database
                    storeAPIResultInDatabase(remoteResult.result)

                    // Return the album values from the database
                    emit(PhotosResult.Success(loadAlbumsFromDatabase()))
                }
            }
        } catch (error: Throwable) {
            e(error) { "Unable to fetch albums" }

            val type =
                if (error is ApiException) PhotosError.RequestFail
                else PhotosError.Error(error)

            emit(PhotosResult.Fail(type))
        }
    }

    suspend fun loadMoreAlbums(): Flow<PhotosResult<List<Album>>> = flow {
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
            emit(PhotosResult.Fail(PhotosError.RequestFail))

            return@flow
        }

        emitSuccess(results)
    }

    private suspend fun loadAlbumsFromDatabase(): List<Album> {
        return albumDao.allAlbums().mapToModel()
    }

    private suspend fun loadInitialAlbums(): PhotosResult<List<Album>> =
        safeApiCall { client ->
            val albumsResponse = client.listAlbums()
            currentPage = albumsResponse.page

            albumsResponse.iterateAll().toModelList()
        }

    private suspend fun storeAPIResultInDatabase(data: List<Album>) {
        val entities = data.toEntities()
        albumDao.insert(entities)
    }
}