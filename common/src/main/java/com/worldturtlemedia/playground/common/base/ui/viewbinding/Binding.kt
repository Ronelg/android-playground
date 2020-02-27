package com.worldturtlemedia.playground.common.base.ui.viewbinding

import androidx.viewbinding.ViewBinding

interface Binding<B: ViewBinding> {

    val binding: B

    fun withBinding(block: B.() -> Unit) {
        binding.apply(block)
    }
}