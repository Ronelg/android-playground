package com.worldturtlemedia.playground.common.ktx

import com.github.ajalt.timberkt.i
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.coroutineContext

suspend fun <T> FlowCollector<T>.emitAndLog(value: T) {
    i { "Emitting: $value" }
    emit(value)
}

suspend inline fun <T> Flow<T>.safeCollect(crossinline action: suspend (T) -> Unit) {
    collect {
        coroutineContext.ensureActive()
        action(it)
    }
}