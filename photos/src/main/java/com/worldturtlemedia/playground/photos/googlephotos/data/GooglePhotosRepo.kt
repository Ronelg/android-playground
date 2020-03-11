package com.worldturtlemedia.playground.photos.googlephotos.data

import com.google.photos.types.proto.Album
import kotlinx.coroutines.flow.*

class GooglePhotosRepo(
    private val factory: PhotosClientFactory
) {

    suspend fun test(): Flow<PhotosResult<List<Album>>> {
        return factory.safeApiCall { client ->

            val result = client.listAlbums()

            val list = result.iterateAll().toList()

            list
        }
    }

    companion object Factory {
        val instance by lazy {
            GooglePhotosRepo(PhotosClientFactory.instance)
        }
    }
}
