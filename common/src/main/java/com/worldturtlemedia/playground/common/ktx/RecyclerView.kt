package com.worldturtlemedia.playground.common.ktx

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

fun RecyclerView.addOnScrollStateDragging(block: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                block()
            }
        }
    })
}