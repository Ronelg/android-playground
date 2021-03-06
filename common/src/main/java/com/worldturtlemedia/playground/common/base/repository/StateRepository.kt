package com.worldturtlemedia.playground.common.base.repository

import com.github.ajalt.timberkt.e
import com.github.ajalt.timberkt.i
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlin.coroutines.CoroutineContext

@OptIn(
    ExperimentalCoroutinesApi::class, FlowPreview::class
)
abstract class StateRepository<V : Any> : CoroutineScope {

    protected val parentJob = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.IO + parentJob

    private val _state = ConflatedBroadcastChannel<V>()

    protected open val debugMode: Boolean = false

    val state: Flow<V>
        get() = _state.asFlow()

    val currentState: V?
        get() = _state.valueOrNull

    protected fun setState(state: V?) {
        when {
            state == null -> log { "Received state was null, doing nothing." }
            _state.isClosedForSend -> onSetStateFailed()
            else -> {
                log { "Offering new state: $state" }
                _state.offer(state)
            }
        }
    }

    protected open fun onSetStateFailed() {
        e { "State Channel is closed for sending" }
    }

    protected fun log(messageBlock: () -> String) {
        i(message = messageBlock)
    }
}