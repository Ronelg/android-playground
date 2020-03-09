package com.worldturtlemedia.playground.common.ktx

import com.github.ajalt.timberkt.i
import kotlinx.coroutines.flow.FlowCollector

suspend fun <T> FlowCollector<T>.emitAndLog(value: T) {
    i { "Emitting: $value" }
    emit(value)
}