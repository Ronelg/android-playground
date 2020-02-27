package com.worldturtlemedia.playground.common.base.repository

import com.github.ajalt.timberkt.e
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

@UseExperimental(ExperimentalCoroutinesApi::class, FlowPreview::class)
abstract class StateRepository<V : Any> {

    private val _state = ConflatedBroadcastChannel<V>()

    val state: Flow<V>
        get() = _state.asFlow()

    val currentState: V?
        get() = _state.valueOrNull

    protected fun setState(state: V?) {
        when {
            state == null -> return
            _state.isClosedForSend -> onSetStateFailed()
            else -> _state.offer(state)
        }
    }

    protected open fun onSetStateFailed() {
        e { "State Channel is closed for sending" }
    }
}