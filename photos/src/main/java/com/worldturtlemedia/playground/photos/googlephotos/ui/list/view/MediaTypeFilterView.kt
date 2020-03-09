package com.worldturtlemedia.playground.photos.googlephotos.ui.list.view

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import com.worldturtlemedia.playground.common.base.ui.viewbinding.Binding
import com.worldturtlemedia.playground.common.base.ui.viewbinding.withBinding
import com.worldturtlemedia.playground.common.ktx.color
import com.worldturtlemedia.playground.common.ktx.onClick
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.FilterMediaTypeViewBinding
import com.worldturtlemedia.playground.photos.databinding.FilterMediaTypeViewBinding.bind

class MediaTypeFilterView : HorizontalScrollView, Binding<FilterMediaTypeViewBinding> {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.filter_media_type_view, this)

        isFillViewport = true
    }

    private var filterClickedListener: (MediaTypeFilter) -> Unit = {}

    override val binding: FilterMediaTypeViewBinding by lazy { bind(findViewById(R.id.container)) }

    private val buttons = withBinding { listOf(btnAll, btnVideos, btnPhotos) }

    override fun onFinishInflate() {
        super.onFinishInflate()

        withBinding {
            btnAll.onClick { filterClickedListener(MediaTypeFilter.All) }
            btnVideos.onClick { filterClickedListener(MediaTypeFilter.Videos) }
            btnPhotos.onClick { filterClickedListener(MediaTypeFilter.Photos) }
        }

        if (isInEditMode) {
            setSelected(MediaTypeFilter.All)
        }
    }

    fun setSelected(mediaType: MediaTypeFilter) {
        val targetView = mapTypeToView(mediaType)
        buttons.forEach { view ->
            val colorRes =
                if (view == targetView) R.color.colorAccent
                else R.color.white

            view.setTextColor(context.color(colorRes))
        }
    }

    fun onFilterClicked(block: (MediaTypeFilter) -> Unit) {
        filterClickedListener = block
    }

    private fun mapTypeToView(type: MediaTypeFilter) = withBinding {
        when (type) {
            is MediaTypeFilter.All -> btnAll
            is MediaTypeFilter.Videos -> btnVideos
            is MediaTypeFilter.Photos -> btnPhotos
        }
    }
}

sealed class MediaTypeFilter {
    object All : MediaTypeFilter()
    object Videos : MediaTypeFilter()
    object Photos : MediaTypeFilter()
}