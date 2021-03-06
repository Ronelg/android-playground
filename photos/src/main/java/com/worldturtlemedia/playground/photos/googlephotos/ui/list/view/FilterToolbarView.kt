package com.worldturtlemedia.playground.photos.googlephotos.ui.list.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.worldturtlemedia.playground.common.ktx.visibleOrGone
import com.worldturtlemedia.playground.photos.BuildConfig
import com.worldturtlemedia.playground.photos.R

class FilterToolbarView : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.filter_toolbar_view, this)
    }

    private val btnFilter by lazy { findViewById<ImageView>(R.id.btnFilter) }

    private val btnDebug by lazy { findViewById<ImageView>(R.id.btnDebug) }

    var onFilterClicked: () -> Unit = {}

    var onDebugClicked: () -> Unit = {}

    override fun onFinishInflate() {
        super.onFinishInflate()

        btnDebug.visibleOrGone = BuildConfig.DEBUG || isInEditMode

        btnFilter.setOnClickListener { onFilterClicked() }
        btnDebug.setOnClickListener { onDebugClicked() }
    }
}