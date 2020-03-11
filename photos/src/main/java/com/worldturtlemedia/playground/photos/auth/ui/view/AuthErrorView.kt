package com.worldturtlemedia.playground.photos.auth.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.worldturtlemedia.playground.common.base.ui.viewbinding.Binding
import com.worldturtlemedia.playground.common.ktx.onClick
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.AuthenticationErrorStubBinding
import com.worldturtlemedia.playground.photos.databinding.AuthenticationErrorStubBinding.bind

class AuthErrorView : ConstraintLayout, Binding<AuthenticationErrorStubBinding> {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.authentication_error_stub, this)
    }

    override val binding: AuthenticationErrorStubBinding by lazy { bind(this) }

    var onRetry: () -> Unit = {}

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding.btnRetry.setOnClickListener { onRetry() }
    }
}