package com.worldturtlemedia.playground.common.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Base class for one-off state events that require a view interaction.
 *
 * ```
 * data class SampleState(..., val event: SampleViewEvent? = null)
 * ```
 *
 * Extending classes are best declared as 'sealed':
 *
 * ```
 * sealed class SampleViewEvent : ViewEvent() {
 *     class ShowSample : SampleViewEvent()
 *     class DoNotCare : SampleViewEvent()
 * }
 * ```
 *
 * Observers can then check for unhandled events like this:
 *
 * ```
 * viewModel.state.observe(this) { state ->
 *     state.event?.consume { event ->
 *         when (event) {
 *             is SampleViewEvent.ShowSample -> {
 *                 // do something
 *                 true
 *             }
 *             is SampleViewEvent.DoNotCare -> false
 *         }
 *     }
 * }
 * ```
 *
 * The `SampleViewEvent.ShowSample` will be "handled" and will not be observable again until a change.
 */
abstract class ViewEvent {

    internal var handled = false

    final override fun equals(other: Any?): Boolean {
        if (other !is ViewEvent) return false

        return other.handled == handled
    }

    final override fun hashCode(): Int = handled.hashCode()
}

fun <T : ViewEvent> T.consume(handled: (event: T) -> Boolean) {
    if (!this.handled) this.handled = handled(this)
}

typealias LiveSingleEvent<T> = LiveData<SingleEvent<T>>

typealias MutableLiveSingleEvent<T> = MutableLiveData<SingleEvent<T>>

data class SingleEvent<T>(private val content: T) : ViewEvent() {

    fun peek(): T = content

    fun peek(block: (T) -> Unit) = block(peek())

    fun consume(): T? {
        return if (handled) null
        else {
            handled = true
            content
        }
    }

    fun consume(block: (T) -> Unit) {
        consume()?.let(block)
    }
}

fun <T : Any> T.asSingleEvent() = SingleEvent(this)

