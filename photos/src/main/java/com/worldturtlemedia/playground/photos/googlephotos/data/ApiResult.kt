package com.worldturtlemedia.playground.photos.googlephotos.data

sealed class ApiResult<out T> {
    data class Fail(val error: ApiError) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
    data class Success<T>(val result: T) : ApiResult<T>()
}

sealed class ApiError {
    object RequestFail : ApiError()
    object NotAuthenticated : ApiError()
    object ClientFailure : ApiError()
    data class Error(val error: Throwable) : ApiError() {

        override fun toString(): String = "Error occurred: ${error.localizedMessage}"
    }
}