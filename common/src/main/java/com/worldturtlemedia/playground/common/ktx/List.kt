package com.worldturtlemedia.playground.common.ktx

import androidx.annotation.CheckResult

/**
 * Return the index of the [element] or return `null` instead of `-1` if the element is not found.
 */
fun <E> List<E>.indexOfOrNull(element: E): Int? = indexOf(element).takeIf { it >= 0 }

/**
 * Replace an element in the list at [index] with [newElement].
 *
 * *Note: Ensure that your index is within the bounds of the list.
 *
 * @throws IndexOutOfBoundsException
 */
fun <E> MutableList<E>.replaceAt(index: Int, newElement: E) {
    this[index] = newElement
}

/**
 * Create a copy of the [List] then replace the element [E] at the [index] with the [newElement].
 *
 * @return Copy of the original list with the element replaced.
 */
@CheckResult
fun <E> List<E>.replaceAt(index: Int, newElement: E): List<E> =
    copy { replaceAt(index, newElement) }

/**
 * Create a copy of the [List] then merge it with [list]
 *
 * @return Copy of the list with the items of [list] added.
 */
@CheckResult
fun <E> List<E>.merge(list: List<E>): List<E> = toMutableList().apply { addAll(list) }.toList()

/**
 * Create a mutable copy of the [List], which can then be mutated using [block].
 *
 * @param[block] [MutableList] scoped lambda for modifying the list.
 * @return Copy of the original list.
 */
@CheckResult
fun <E> List<E>.copy(block: MutableList<E>.() -> Unit): List<E> =
    toMutableList().apply(block).toList()
