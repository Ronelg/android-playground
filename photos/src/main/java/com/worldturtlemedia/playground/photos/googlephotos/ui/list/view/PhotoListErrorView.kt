package com.worldturtlemedia.playground.photos.googlephotos.ui.list.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.worldturtlemedia.playground.common.base.ui.viewbinding.Binding
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.PhotosListErrorStubBinding
import com.worldturtlemedia.playground.photos.databinding.PhotosListErrorStubBinding.bind

class PhotoListErrorView : ConstraintLayout, Binding<PhotosListErrorStubBinding> {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.photos_list_error_stub, this)
    }

    override val binding: PhotosListErrorStubBinding by lazy { bind(this) }

    var onRetry: () -> Unit = {}

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding.btnRetry.setOnClickListener { onRetry() }
    }

    fun setErrorText(text: String?) {
        binding.txtErrorText.text = text ?: ""
    }
}