package com.worldturtlemedia.playground.common.ktx

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup

var View.visibleOrGone
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

var View.visible
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.INVISIBLE
    }

var View.invisible
    get() = visibility == View.INVISIBLE
    set(value) {
        visibility = if (value) View.INVISIBLE else View.VISIBLE
    }

var View.gone
    get() = visibility == View.GONE
    set(value) {
        visibility = if (value) View.GONE else View.VISIBLE
    }

fun View.onClick(block: () -> Unit) {
    setOnClickListener { block() }
}

fun View.onLongClick(block: () -> Boolean) {
    setOnLongClickListener { block() }
}

fun View.addRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun View.addCircleRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
    setBackgroundResource(resourceId)
}

fun ViewGroup.addView(block: View.() -> View) {
    addView(run(block))
}