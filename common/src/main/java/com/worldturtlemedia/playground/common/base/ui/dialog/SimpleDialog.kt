package com.worldturtlemedia.playground.common.base.ui.dialog

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class SimpleDialog<B: ViewBinding>(
    @LayoutRes layout: Int
) : BaseDialog<B, Unit>(layout) {

    protected fun confirm() {
        confirm(Unit)
    }
}

suspend fun <B: ViewBinding> SimpleDialog<B>.showAsync(fragment: Fragment) {
    fragment.showDialogForResult(this)
}