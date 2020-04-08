package com.worldturtlemedia.playground.photos.googlephotos.data

sealed class ApiResult<out T> {
    data class Fail(val error: ApiError) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
    data class Success<T>(val data: T) : ApiResult<T>()

    companion object {
        val requestFail = Fail(ApiError.RequestFail)
        val clientFail = Fail(ApiError.ClientFailure)
        fun error(throwable: Throwable) = Fail(ApiError.Error(throwable))
    }
}

sealed class ApiError(val message: String) {
    object RequestFail : ApiError("Failed to complete the request")
    object ClientFailure : ApiError("Failed to create the photos Client")
    object Unauthenticated : ApiError("Unauthenticated")
    data class Error(val error: Throwable) : ApiError(error.message ?: "Unknown error") {

        override fun toString(): String = "Error occurred: ${error.localizedMessage}"
    }
}

fun Throwable.asApiError() = ApiResult.Fail(ApiError.Error(this))

fun <T> ApiResult<T>.dataOrNull(): T? = (this as? ApiResult.Success)?.data