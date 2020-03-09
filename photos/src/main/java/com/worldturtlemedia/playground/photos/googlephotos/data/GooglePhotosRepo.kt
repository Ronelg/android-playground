package com.worldturtlemedia.playground.photos.googlephotos.data

import com.github.ajalt.timberkt.i
import com.google.api.gax.rpc.ApiException
import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.types.proto.Album
import com.worldturtlemedia.playground.common.ktx.emitAndLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

class GooglePhotosRepo(
    private val client: PhotosClientFactory
) {

    suspend fun test() {
        client.safeApiCall<List<Album>> { client ->
            client.listAlbums().iterateAll().toList()
        }
    }

    companion object Factory {
        val instance by lazy {
            GooglePhotosRepo(PhotosClientFactory.instance)
        }
    }
}
