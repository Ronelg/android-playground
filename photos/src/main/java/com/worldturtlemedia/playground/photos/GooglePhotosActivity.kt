package com.worldturtlemedia.playground.photos

import androidx.lifecycle.lifecycleScope
import com.worldturtlemedia.playground.common.base.ui.BaseActivity
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.photos.databinding.GooglePhotosActivityBinding
import com.worldturtlemedia.playground.photos.databinding.GooglePhotosActivityBinding.inflate
import com.worldturtlemedia.playground.photos.firebase.ClientSecretSource

class GooglePhotosActivity : BaseActivity<GooglePhotosActivityBinding>() {

    override val binding: GooglePhotosActivityBinding by viewBinding { inflate(it) }

    init {
        // Grab the Client-secret key from Firebase
        // Ideally this would be done on a backend when the user check-ins
        lifecycleScope.launchWhenCreated {
            ClientSecretSource.instance.init()
        }
    }
}
