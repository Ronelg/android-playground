package com.worldturtlemedia.playground.photos.googlephotos.data.library

import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import org.joda.time.DateTime

private typealias Cache = LinkedHashMap<MediaItem, DateTime>

class MemoryMediaItemCache : MediaItemCache {

    companion object Factory {
        val instance by lazy { MemoryMediaItemCache() }
    }

    private val cache: Cache = linkedMapOf()

    override suspend fun getAllItems(): List<MediaItem> {
        clearExpired()

        return cache.entries
            .filter { (_, timestamp) -> isValid(timestamp) }
            .map { (item, _) -> item }
    }

    override suspend fun storeItems(items: List<MediaItem>) {
        items.forEach { mediaItem ->
            cache[mediaItem] = DateTime.now()
        }

        clearExpired()
    }

    override suspend fun clearExpired() {
        for ((item, timestamp) in cache.entries) {
            if (!isValid(timestamp)) {
                cache.remove(item)
            }
        }
    }

    override suspend fun clear() = cache.clear()

    private fun isValid(timestamp: DateTime): Boolean {
        return timestamp.plusMillis(expiryMillis).isAfterNow
    }
}