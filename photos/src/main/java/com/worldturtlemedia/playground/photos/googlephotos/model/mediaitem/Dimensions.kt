package com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem

data class Dimensions(
    val width: Long,
    val height: Long
) {

    val orientation = when {
        width > height -> Orientation.Landscape
        width < height -> Orientation.Portrait
        else -> Orientation.Square
    }
}