package com.worldturtlemedia.playground.photos.googlephotos.data.library

import com.worldturtlemedia.playground.common.ktx.minutes
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem

interface MediaItemCache {

    /**
     * This value will determine how long to keep the [MediaItem]s cached for.
     *
     * The value will be treated as milliseconds and compared to the current time.
     *
     * According to the Google Photos docs, a retrieved [MediaItem] is valid for 60 minutes.
     * The default value here of 45 minutes should give the user some time to look through the
     * photos and videos.
     */
    val expiryMillis: Int
        get() = 45.minutes

    suspend fun hasItems(): Boolean = getAllItems().isNotEmpty()

    suspend fun getAllItems(): List<MediaItem>

    suspend fun storeItems(items: List<MediaItem>)

    /**
     * Clear only the expired [MediaItem]s based on the [expiryMillis].
     */
    suspend fun clearExpired()

    /**
     * Clear all of the [MediaItem]s from the cache.
     */
    suspend fun clear()
}