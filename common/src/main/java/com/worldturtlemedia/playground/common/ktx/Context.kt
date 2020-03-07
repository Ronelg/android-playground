package com.worldturtlemedia.playground.common.ktx

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

fun Context.color(@ColorRes res: Int) = ContextCompat.getColor(this, res)

fun Context.dimen(@DimenRes res: Int): Float = resources.getDimension(res)

fun Context.pixelDimen(@DimenRes res: Int): Int = resources.getDimensionPixelSize(res)

fun Context.string(@StringRes res: Int): String = resources.getString(res)

fun Context.string(@StringRes res: Int, vararg formatArgs: Any): String =
    resources.getString(res, formatArgs)

inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    block(intent)
    startActivity(intent)
}