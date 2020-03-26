package com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem

sealed class Orientation {
    object Landscape : Orientation()
    object Portrait : Orientation()
    object Square : Orientation()
}