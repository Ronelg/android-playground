package com.worldturtlemedia.playground.photos.googlephotos.ui.list.items

import android.view.View
import com.worldturtlemedia.playground.common.base.ui.groupie.ViewBindingItem
import com.worldturtlemedia.playground.common.ktx.context
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.MediaItemListHeaderBinding
import com.worldturtlemedia.playground.photos.databinding.MediaItemListHeaderBinding.bind
import com.xwray.groupie.Item

data class MediaItemListHeader(
    private val dateString: String
) : ViewBindingItem<MediaItemListHeaderBinding>() {

    override fun getLayout(): Int = R.layout.media_item_list_header

    override fun inflate(itemView: View): MediaItemListHeaderBinding = bind(itemView)

    override fun bind(viewBinding: MediaItemListHeaderBinding, position: Int) {
        with(viewBinding) {
            txtDate.text = dateString
        }
    }

    override fun getId(): Long = dateString.hashCode().toLong()
}