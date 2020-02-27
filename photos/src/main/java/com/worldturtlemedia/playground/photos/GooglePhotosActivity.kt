package com.worldturtlemedia.playground.photos

import com.worldturtlemedia.playground.common.base.ui.BaseActivity
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.photos.databinding.GooglePhotosActivityBinding
import com.worldturtlemedia.playground.photos.databinding.GooglePhotosActivityBinding.inflate

class GooglePhotosActivity : BaseActivity<GooglePhotosActivityBinding>() {

    override val binding: GooglePhotosActivityBinding by viewBinding { inflate(it) }

}
