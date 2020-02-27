package com.worldturtlemedia.playground.ui.main

import com.worldturtlemedia.playground.common.base.ui.BaseActivity
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)
}
