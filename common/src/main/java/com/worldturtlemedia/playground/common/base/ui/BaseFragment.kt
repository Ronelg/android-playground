package com.worldturtlemedia.playground.common.base.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B: ViewBinding>(
    @LayoutRes val layoutRes: Int
) : Fragment(layoutRes) {

    abstract val binding: B

    protected val owner: LifecycleOwner
        get() = viewLifecycleOwner

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupViews()
        observeViewModel()
    }

    protected open fun setupViews() {}

    protected open fun observeViewModel() {}

    protected fun withBinding(block: B.() -> Unit) {
        binding.apply(block)
    }
}