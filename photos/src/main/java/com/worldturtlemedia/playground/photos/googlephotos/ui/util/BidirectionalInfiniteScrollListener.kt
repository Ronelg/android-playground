package com.worldturtlemedia.playground.photos.googlephotos.ui.util

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ajalt.timberkt.e

typealias OnLoadMore = () -> Unit

// TODO: Investigate using the paging library instead?
class BidirectionalInfiniteScrollListener(
    private val layoutManager: GridLayoutManager
) : RecyclerView.OnScrollListener() {

    private val visibleThreshold: Int = 5* layoutManager.spanCount

    var canLoadAny: Boolean = true

    var isLoadingTop: Boolean = false

    var isLoadingBottom: Boolean = false

    private var loadMoreTopListener: OnLoadMore = {}

    private var loadMoreBottomListener: OnLoadMore = {}

    fun onLoadMoreTop(block: OnLoadMore) {
        loadMoreTopListener = block
    }

    fun onLoadMoreBottom(block: OnLoadMore) {
        loadMoreBottomListener = block
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (canLoadAny) {
            checkIfLoadTop()
            checkIfLoadBottom(dy)
        }
    }

    private fun checkIfLoadTop() {
        val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        val needsToLoadMore = (firstVisibleItemPosition - visibleThreshold) <= 0
        if (!isLoadingTop && needsToLoadMore) {
            isLoadingTop = true
            loadMoreTopListener()
        }
    }

    private fun checkIfLoadBottom(dy: Int) {
        if (dy <= 0) return

        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

        val totalItemCount = layoutManager.itemCount
        val needsToLoadMore = totalItemCount <= lastVisibleItemPosition + visibleThreshold

        if (!isLoadingBottom && needsToLoadMore) {
            isLoadingBottom = true
            loadMoreBottomListener()
        }
    }
}