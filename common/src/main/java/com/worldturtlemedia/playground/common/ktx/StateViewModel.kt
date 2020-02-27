package com.worldturtlemedia.playground.common.ktx

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.observe
import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.core.SingleEvent

fun <S : State, Property> LiveData<S>.observeProperty(
    owner: LifecycleOwner,
    property: (S) -> Property,
    onChange: (value: Property) -> Unit
) {
    map(property).distinctUntilChanged().observe(owner, onChange)
}

fun <S : State, Property> LiveData<S>.observePropertyChange(
    owner: LifecycleOwner,
    property: (S) -> Property,
    onChange: (state: S) -> Unit
) {
    map(property).distinctUntilChanged().observe(owner) { value?.let(onChange) }
}

/**
 * Similar to [observeProperty] however it will let you observe multiple properties.
 */
fun <S : State, Property1, Property2> LiveData<S>.observePropertyPair(
    owner: LifecycleOwner,
    properties: (S) -> Pair<Property1, Property2>,
    onChange: (value: Pair<Property1, Property2>) -> Unit
) {
    map(properties).distinctUntilChanged().observe(owner, onChange)
}

fun <S : State, T> LiveData<S>.observeUnconsumedProperty(
    owner: LifecycleOwner,
    property: (S) -> SingleEvent<T>,
    onConsume: (value: T) -> Unit
) {
    map(property).observeUnConsumed(owner, onConsume)
}