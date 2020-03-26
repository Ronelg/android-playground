package com.worldturtlemedia.playground.photos.googlephotos.model

const val MEDIA_THUMBNAIL_WIDTH = 480
const val MEDIA_THUMBNAIL_HEIGHT = 480

fun createMediaThumbnailUrl(
    url: String,
    width: Int = MEDIA_THUMBNAIL_WIDTH,
    height: Int = MEDIA_THUMBNAIL_HEIGHT
) = "$url=w$width-h$height"
