package com.worldturtlemedia.playground.ui.main

import com.worldturtlemedia.playground.R
import com.worldturtlemedia.playground.common.base.ui.BaseFragment
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.common.ktx.onClick
import com.worldturtlemedia.playground.databinding.MainFragmentBinding

class MainFragment : BaseFragment(R.layout.main_fragment) {

    private val binding by viewBinding(MainFragmentBinding::bind)

    override fun setupViews() = with(binding) {
        btnGooglePhotos.onClick { }
    }
}
