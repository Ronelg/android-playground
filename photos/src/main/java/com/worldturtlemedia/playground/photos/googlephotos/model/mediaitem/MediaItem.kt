package com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem

import com.worldturtlemedia.playground.photos.googlephotos.model.createMediaThumbnailUrl
import org.joda.time.DateTime

sealed class MediaItem {
    abstract val id: String
    abstract val baseUrl: String
    abstract val dimensions: Dimensions
    abstract val mimeType: String
    abstract val creationTime: DateTime

    abstract val downloadUrl: String

    val thumbnailUrl: String
        get() = createMediaThumbnailUrl(baseUrl)
}

data class VideoItem(
    override val id: String,
    override val baseUrl: String,
    override val dimensions: Dimensions,
    override val mimeType: String,
    override val creationTime: DateTime,
    val fps: Double
) : MediaItem() {

    override val downloadUrl: String = "$baseUrl=d"
}

data class PhotoItem(
    override val id: String,
    override val baseUrl: String,
    override val dimensions: Dimensions,
    override val mimeType: String,
    override val creationTime: DateTime
) : MediaItem() {

    override val downloadUrl: String = "$baseUrl=dv"
}

val MediaItem.isVideo: Boolean
    get() = this is VideoItem

val MediaItem.isPhoto: Boolean
    get() = this is PhotoItem