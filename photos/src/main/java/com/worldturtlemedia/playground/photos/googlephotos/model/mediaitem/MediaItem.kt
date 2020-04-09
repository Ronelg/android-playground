package com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem

import com.worldturtlemedia.playground.photos.googlephotos.model.createMediaThumbnailUrl
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.MediaFilter
import org.joda.time.DateTime
import org.joda.time.LocalDate

sealed class MediaItem {
    abstract val id: String
    abstract val baseUrl: String
    abstract val dimensions: Dimensions
    abstract val mimeType: String
    abstract val creationTime: DateTime

    abstract fun downloadUrl(): String

    fun thumbnailUrl(): String = createMediaThumbnailUrl(baseUrl)
}

val MediaItem.createdDate: LocalDate
    get() = creationTime.toLocalDate()

data class VideoItem(
    override val id: String,
    override val baseUrl: String,
    override val dimensions: Dimensions,
    override val mimeType: String,
    override val creationTime: DateTime,
    val fps: Double
) : MediaItem() {

    override fun downloadUrl(): String = "$baseUrl=d"
}

data class PhotoItem(
    override val id: String,
    override val baseUrl: String,
    override val dimensions: Dimensions,
    override val mimeType: String,
    override val creationTime: DateTime
) : MediaItem() {

    override fun downloadUrl(): String = "$baseUrl=dv"
}

val MediaItem.isVideo: Boolean
    get() = this is VideoItem

val MediaItem.isPhoto: Boolean
    get() = this is PhotoItem

fun MediaItem.matchesFilter(filter: MediaFilter) = when(filter) {
    MediaFilter.All -> true
    MediaFilter.Video -> isVideo
    MediaFilter.Photo -> isPhoto
}