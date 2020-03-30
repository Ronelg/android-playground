package com.worldturtlemedia.playground.photos.googlephotos.data.library

import com.worldturtlemedia.playground.photos.googlephotos.data.PhotosClient
import com.worldturtlemedia.playground.photos.googlephotos.data.PhotosClientFactory

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
}