package com.worldturtlemedia.playground.photos.googlephotos.data

sealed class PhotosResult<out T> {
    data class Fail(val error: PhotosError) : PhotosResult<Nothing>()
    object Loading : PhotosResult<Nothing>()
    data class Success<T>(val result: T) : PhotosResult<T>()
}

sealed class PhotosError {
    object RequestFail : PhotosError()
    object NotAuthenticated : PhotosError()
    object ClientFailure : PhotosError()
    data class Error(val error: Throwable) : PhotosError()
}