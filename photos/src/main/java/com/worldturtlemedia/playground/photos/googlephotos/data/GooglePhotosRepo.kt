package com.worldturtlemedia.playground.photos.googlephotos.data

import com.google.photos.types.proto.Album
import kotlinx.coroutines.flow.Flow

class GooglePhotosRepo(
    private val delegate: PhotosClientFactory
) : PhotosClient by delegate {

    suspend fun test(): Flow<PhotosResult<List<Album>>> = safeApiCall { client ->
        val result = client.listAlbums()

        val list = result.iterateAll().toList()

        list
    }

    companion object Factory {
        val instance by lazy {
            GooglePhotosRepo(PhotosClientFactory.instance)
        }
    }
}
