package com.worldturtlemedia.playground.common.base.ui.dialog

import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding

abstract class SimpleDialog<B: ViewBinding>(
    @LayoutRes layout: Int
) : BaseDialog<B, Unit>(layout) {

    protected fun confirm() {
        confirm(Unit)
    }
}