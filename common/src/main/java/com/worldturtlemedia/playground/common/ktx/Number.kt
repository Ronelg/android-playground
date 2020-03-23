package com.worldturtlemedia.playground.common.ktx

import android.content.res.Resources

fun even(value: Int): Boolean = value % 2 == 0

fun odd(value: Int): Boolean = !even(value)

val Int.dp: Int
    get() = toFloat().dp.toInt()

val Float.dp: Float
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f)


fun String?.parseLongOrNull(): Long? = try {
    this?.toLong()
} catch (error: Throwable) {
    null
}

fun String?.parseLong(default: Long) = parseLongOrNull() ?: default