package com.worldturtlemedia.playground.photos.googlephotos.data.db.mediaitem

import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.PhotoItem
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.VideoItem
import org.joda.time.DateTime

interface MediaItemEntity {
    val primaryKey: Int

    val item: MediaItem

    val createdAt: Long
}

fun List<MediaItemEntity>.toModels(): List<MediaItem> = map { it.item }

fun MediaItem.toEntity() = when (this) {
    is VideoItem -> VideoItemEntity(id.hashCode(), this, DateTime.now().millis)
    is PhotoItem -> PhotoItemEntity(id.hashCode(), this, DateTime.now().millis)
}

fun List<MediaItem>.toEntities() = map { it.toEntity() }

fun List<MediaItemEntity>.splitByEntityType(): Pair<List<VideoItemEntity>, List<PhotoItemEntity>> {
    val videos = filterIsInstance<VideoItemEntity>()
    val photos = filterIsInstance<PhotoItemEntity>()

    return videos to photos
}