package com.worldturtlemedia.playground.photos.list

import com.worldturtlemedia.playground.common.base.ui.BaseFragment
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.PhotosListFragmentBinding
import com.worldturtlemedia.playground.photos.databinding.PhotosListFragmentBinding.bind

class PhotosListFragment : BaseFragment<PhotosListFragmentBinding>(R.layout.photos_list_fragment) {

    override val binding: PhotosListFragmentBinding by viewBinding { bind(it) }
}