package com.worldturtlemedia.playground.photos.googlephotos.data

sealed class PhotosResult<out T> {
    data class Fail(val error: Throwable) : PhotosResult<Nothing>(), Error
    object NotAuthenticated : PhotosResult<Nothing>(), Error
    object Loading : PhotosResult<Nothing>()
    data class Success<T>(val result: T) : PhotosResult<T>()

    interface Error
}