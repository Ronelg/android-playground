package com.worldturtlemedia.playground.photos.googlephotos.data.library

import com.worldturtlemedia.playground.photos.db.PhotosDatabase
import com.worldturtlemedia.playground.photos.googlephotos.data.db.mediaitem.*
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.joda.time.DateTime

class DatabaseMediaItemCache(
    private val dao: MediaItemDao
) : MediaItemCache {

    companion object Factory {

        val instance by lazy {
            DatabaseMediaItemCache(PhotosDatabase.getInstance().mediaItemDao())
        }
    }

    override suspend fun getAllItems(): List<MediaItem> {
        val items = getAllMediaItemEntities()
        deleteExpired(items.expired)

        return items.valid.toModels().sortedBy { it.creationTime }
    }

    override suspend fun storeItems(items: List<MediaItem>) {
        val (videos, photos) = items.toEntities().splitByEntityType()

        withContext(Dispatchers.IO) {
            awaitAll(
                async { dao.insertVideoItems(videos) },
                async { dao.insertPhotoItems(photos) }
            )
        }
    }

    override suspend fun clearExpired() {
        val expiredList = getAllMediaItemEntities().expired
        deleteExpired(expiredList)
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            awaitAll(
                async { dao.deleteAllVideoItems() },
                async { dao.deleteAllPhotoItems() }
            )
        }
    }

    private suspend fun getAllMediaItemEntities(): ValidEntityPair =
        withContext(Dispatchers.IO) {
            val videos = async { dao.allVideoItems() }
            val photos = async { dao.allPhotoItems() }

            val entities = awaitAll(videos, photos).flatten()
            val valid = entities.filterValid()
            val invalid = entities.filter { !isValid(it) }

            ValidEntityPair(valid, invalid)
        }

    private suspend fun deleteExpired(expired: List<MediaItemEntity>) =
        withContext(Dispatchers.IO) {
            val (videos, photos) = expired.splitByEntityType()

            awaitAll(
                async { dao.deleteVideoItems(videos) },
                async { dao.deletePhotoItems(photos) }
            )
        }

    private fun List<MediaItemEntity>.filterValid() = filter { isValid(it) }

    private fun isValid(entity: MediaItemEntity): Boolean {
        val createdTimestamp = DateTime(entity.createdAt)
        return createdTimestamp.plusMillis(expiryMillis).isAfterNow
    }
}

private data class ValidEntityPair(
    val valid: List<MediaItemEntity>,
    val expired: List<MediaItemEntity>
)