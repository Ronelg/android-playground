package com.worldturtlemedia.playground.photos.googlephotos.model

import com.google.photos.types.proto.Album as GoogleAlbum

data class Album(
    val id: String,
    val title: String,
    val productUrl: String,
    val itemCount: Long,
    val coverPhotoUrl: String
)

fun GoogleAlbum.map() = Album(
    id = id,
    title = title,
    productUrl = productUrl,
    itemCount = mediaItemsCount,
    coverPhotoUrl = coverPhotoBaseUrl
)

fun Iterable<GoogleAlbum>.toModelList() = map { album -> album.map() }