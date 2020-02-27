package com.worldturtlemedia.playground.common.base.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

abstract class BaseFragment(
    @LayoutRes val layoutRes: Int
) : Fragment(layoutRes) {

    protected val owner: LifecycleOwner
        get() = viewLifecycleOwner

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupViews()
        observeViewModel()
    }

    protected open fun setupViews() {}

    protected open fun observeViewModel() {}
}