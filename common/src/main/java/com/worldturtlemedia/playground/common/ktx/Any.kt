package com.worldturtlemedia.playground.common.ktx

import com.github.ajalt.timberkt.Timber.e

val <T : Any> T?.simpleName: String
    get() = this?.let { it::class.java.simpleName } ?: ""

/**
 * Cast an [Any] object into the reified [R].
 *
 * A convenience wrapper around the safe cast operator.
 *
 * ```
 * interface Invalidate {
 *     fun invalidate()
 * }
 *
 * // Old way
 * (sampleObject as? Invalidate)?.invalidate()
 *
 * // With this helper
 * sampleObject.cast<Invalidate>?.invalidate()
 * ```
 *
 * @param[R] Return type of the cast.
 * @return [Any] casted to [R], or a null.
 */
inline fun <reified R> Any.cast(): R? = this as? R

inline fun <reified R> Any.safeCast(): R? = try {
    cast<R>()
} catch (error: Throwable) {
    e(error) { "Unable to cast $simpleName to ${R::class.java.simpleName}" }
    null
}

fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)