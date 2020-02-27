package com.worldturtlemedia.playground.photos.list.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.worldturtlemedia.playground.common.base.ui.viewbinding.Binding
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.FilterToolbarViewBinding
import com.worldturtlemedia.playground.photos.databinding.FilterToolbarViewBinding.bind

class FilterToolbarView : ConstraintLayout, Binding<FilterToolbarViewBinding> {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.filter_toolbar_view, this)
    }

    override val binding: FilterToolbarViewBinding by lazy { bind(this) }

    var onFilterClicked: () -> Unit = {}

    override fun onFinishInflate() {
        super.onFinishInflate()
    }
}