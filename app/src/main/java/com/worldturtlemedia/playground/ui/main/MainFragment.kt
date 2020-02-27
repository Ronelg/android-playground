package com.worldturtlemedia.playground.ui.main

import com.worldturtlemedia.playground.R
import com.worldturtlemedia.playground.common.base.ui.BaseFragment
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.common.ktx.onClick
import com.worldturtlemedia.playground.databinding.MainFragmentBinding
import com.worldturtlemedia.playground.databinding.MainFragmentBinding.bind

class MainFragment : BaseFragment<MainFragmentBinding>(R.layout.main_fragment) {

    override val binding: MainFragmentBinding by viewBinding { bind(it) }

    override fun setupViews() = withBinding {
        btnGooglePhotos.onClick { }
    }
}
