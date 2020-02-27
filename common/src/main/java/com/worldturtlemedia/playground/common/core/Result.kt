package com.worldturtlemedia.playground.common.core

import com.worldturtlemedia.playground.common.BuildConfig
import kotlinx.coroutines.CancellationException
import timber.log.Timber

sealed class Result<out R> {

    data class Success<out T>(val data: T) : Result<T>()

    data class Error(
        val exception: Throwable,
        private val log: Boolean = false
    ) : Result<Nothing>() {

        init {
            if (log) {
                if (exception !is CancellationException || BuildConfig.DEBUG) {
                    Timber.e(exception)
                }
            }
        }
    }

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}