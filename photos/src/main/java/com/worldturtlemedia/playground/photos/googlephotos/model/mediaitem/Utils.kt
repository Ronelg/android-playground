package com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem

import com.github.ajalt.timberkt.i
import com.google.photos.types.proto.MediaMetadata
import com.google.photos.types.proto.VideoProcessingStatus
import org.joda.time.DateTime
import com.google.photos.types.proto.MediaItem as ProtoMediaItem

fun ProtoMediaItem.toModel(): MediaItem? = mediaMetadata?.run {
    when {
        hasVideo() && hasPhoto() -> null // TODO: This is a "LivePhoto"
        hasVideo() -> createVideoItem()
        hasPhoto() -> createPhotoItem()
        else -> null
    }
}

fun List<ProtoMediaItem>.toModels() = mapNotNull { it.toModel() }

private fun ProtoMediaItem.createVideoItem(): VideoItem? {
    if (mediaMetadata.video.status != VideoProcessingStatus.READY) {
        i { "Video $filename is not processed yet!" }
        return null
    }

    return VideoItem(
        id = id,
        baseUrl = baseUrl,
        dimensions = mediaMetadata.extractDimensions,
        mimeType = mimeType,
        creationTime = DateTime(mediaMetadata.creationTime),
        fps = mediaMetadata.video.fps
    )
}

private fun ProtoMediaItem.createPhotoItem() = PhotoItem(
    id = id,
    baseUrl = baseUrl,
    dimensions = mediaMetadata.extractDimensions,
    mimeType = mimeType,
    creationTime = DateTime(mediaMetadata.creationTime)
)

private val MediaMetadata.extractDimensions: Dimensions
    get() = Dimensions(width = width, height = height)