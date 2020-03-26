package com.worldturtlemedia.playground.photos.googlephotos.ui.list.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.worldturtlemedia.playground.common.ktx.addRipple
import com.worldturtlemedia.playground.common.ktx.addView
import com.worldturtlemedia.playground.common.ktx.color
import com.worldturtlemedia.playground.common.ktx.dp
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.MediaFilter

class MediaTypeFilterView : HorizontalScrollView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    init {
        isFillViewport = true
    }

    private var filterClickedListener: (MediaFilter) -> Unit = {}

    private val buttons = listOf(MediaFilter.All, MediaFilter.Video, MediaFilter.Photo)
        .map { type -> type to createTextView(type) }
        .toMap()

    override fun onFinishInflate() {
        super.onFinishInflate()

        addView {
            LinearLayout(context).apply {
                setBackgroundColor(context.color(R.color.backgroundLighter))
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    context.resources.getDimensionPixelSize(R.dimen.toolbar_height)
                )

                buttons.values.forEach { addView(it) }
            }
        }

        if (isInEditMode) {
            setSelected(MediaFilter.All)
        }
    }

    fun setSelected(mediaType: MediaFilter) {
        buttons.forEach { (type, view) ->
            val colorRes =
                if (mediaType == type) R.color.colorAccent
                else R.color.white

            view.setTextColor(context.color(colorRes))
        }
    }

    fun onFilterClicked(block: (MediaFilter) -> Unit) {
        filterClickedListener = block
    }

    private fun createTextView(type: MediaFilter) = TextView(context).apply {
        setText(type.stringRes)
        setTextColor(context.color(R.color.white))
        addRipple()
        layoutParams = LinearLayout.LayoutParams(80.dp, ViewGroup.LayoutParams.MATCH_PARENT)
        gravity = Gravity.CENTER

        setOnClickListener { filterClickedListener(type) }
    }
}