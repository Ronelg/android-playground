package com.worldturtlemedia.playground.common.base.ui.viewmodel

import androidx.lifecycle.*
import com.github.ajalt.timberkt.i
import com.worldturtlemedia.playground.common.ktx.mediatorLiveDataOf
import com.worldturtlemedia.playground.common.ktx.observeProperty
import com.worldturtlemedia.playground.common.ktx.simpleName
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach

interface State

abstract class StateViewModel<S : State>(
    initialState: S,
    protected open val isDebugMode: Boolean = false
) : ViewModel() {

    /**
     * Create a Single source of truth for the state by hiding the mutable LiveData.
     */
    private val _state = mediatorLiveDataOf(initialState)
    val state: LiveData<S>
        get() = _state

    /**
     * Convenient accessor to get the current value of the state.  We know the state cannot be
     * null, because we pass an initial state in the constructor.
     */
    val currentState: S
        get() = state.value!!

    fun observe(owner: LifecycleOwner, block: (state: S) -> Unit) = state.observe(owner, block)

    fun <Property> observeProperty(
        owner: LifecycleOwner,
        property: (S) -> Property,
        onChange: (value: Property) -> Unit
    ) = state.observeProperty(owner, property, onChange)

    fun onStateChange(
        owner: LifecycleOwner,
        block: (state: S) -> Unit
    ) = observe(owner, block)

    fun <Property> onStateChange(
        owner: LifecycleOwner,
        property: (S) -> Property,
        onChange: (value: S) -> Unit
    ) {
        val current = state
        current.map(property).distinctUntilChanged().observe(owner) { current.value?.let(onChange) }
    }

    /**
     * Create an [actor] that will consume each [Update], invoke the lambda, then update the state
     * if needed.
     *
     * An [actor] is used to prevent race-conditions, and stale [State] being sent to the [Update].
     */
    @OptIn(
        ObsoleteCoroutinesApi::class, ExperimentalCoroutinesApi::class
    )
    private val updateStateActor = viewModelScope.actor<Update<S>>(capacity = Channel.UNLIMITED) {
        channel.consumeEach { update ->
            update.invoke(currentState)
                ?.let { newState ->
                    if (isDebugMode) {
                        i { "${this@StateViewModel.simpleName}: New state: $newState" }
                    }
                    _state.value = newState
                }
                ?: i { "Returned null, not updating state" }
        }
    }

    /**
     * Adapt a [LiveData] into state updates.
     *
     * This is useful when consuming a flow or [LiveData] from another source, like a Repository.
     *
     * @see MediatorLiveData.addSource
     * @param[source] LiveData to adapt.
     * @param[onChange] Lambda expression to convert the [source] data to a [S].
     */
    protected fun <T> addStateSource(source: LiveData<T>, onChange: suspend S.(data: T) -> S?) {
        _state.addSource(source) { data ->
            setState { onChange(currentState, data) }
        }
    }

    /**
     * Wrapper for [MediatorLiveData.removeSource].
     *
     * @param[source] Source to remove.
     */
    protected fun <V> removeStateSource(source: LiveData<V>): Unit = _state.removeSource(source)

    /**
     * Offer an update to the [updateStateActor].
     *
     * Instead of updating the [_state] right away, we instead use an [actor] that will consume
     * the "update" events sequentially, so we can avoid race-conflicts.  And the [block] will always
     * be called with the latest state.
     *
     * @param[block] A lambda scoped to the current [State].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    protected fun setState(block: Update<S>) {
        if (updateStateActor.isClosedForSend) {
            i { "State actor is closed, cannot offer update." }
        } else {
            updateStateActor.offer(block)
        }
    }

    protected fun noUpdate() = null
}

fun <S : State> StateViewModel<S>.launchIO(
    block: suspend CoroutineScope.() -> Unit
) = viewModelScope.launch(Dispatchers.IO, block = block)

typealias Update<S> = suspend S.() -> S?

private fun <VM : StateViewModel<S2>, S1 : State, S2 : State> StateViewModel<S1>.mergeState(
    viewModel: VM
) = MediatorLiveData<Pair<S1?, S2?>>().apply {
    addSource(state) { state1 -> value = state1 to value?.second }
    addSource(viewModel.state) { state2 -> value = value?.first to state2 }
}

fun <VM : StateViewModel<S2>, S1 : State, S2 : State> StateViewModel<S1>.observeMergedState(
    owner: LifecycleOwner,
    viewModel: VM,
    onChange: (S1, S2) -> Unit
) = mergeState(viewModel).observe(owner) { (state1, state2) ->
    if (state1 != null && state2 != null) {
        onChange(state1, state2)
    }
}
