package com.worldturtlemedia.playground.common.ktx

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

fun createGridLayoutManager(
    context: Context,
    spanSize: Int,
    block: GridLayoutManager.() -> Unit = {}
) = GridLayoutManager(context, spanSize).apply(block)

fun GridLayoutManager.buildSpanSizeLookup(block: (position: Int) -> Int) {
    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return block(position)
        }
    }
}