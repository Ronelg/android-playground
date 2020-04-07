package com.worldturtlemedia.playground.photos.googlephotos.data.library

import com.worldturtlemedia.playground.common.ktx.isEqualOrAfter
import com.worldturtlemedia.playground.common.ktx.isEqualOrBefore
import com.worldturtlemedia.playground.common.ktx.minutes
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import org.joda.time.LocalDate

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

    suspend fun getByDate(date: LocalDate) = getAllItems().filter { mediaItem ->
        mediaItem.creationTime.toLocalDate() == date
    }

    suspend fun getAllWithin(start: LocalDate, end: LocalDate): List<MediaItem> {
        if (end.isBefore(start)) {
            throw IllegalArgumentException("Start: $start, has to be before End: $end")
        }

        return getAllItems().filter { mediaItem ->
            val date = mediaItem.creationTime.toLocalDate()
            date.isEqualOrAfter(start) && date.isEqualOrBefore(end)
        }
    }

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