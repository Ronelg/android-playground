package com.worldturtlemedia.playground.photos.googlephotos.ui.list.items

import android.view.View
import com.worldturtlemedia.playground.common.base.ui.groupie.ViewBindingItem
import com.worldturtlemedia.playground.common.ktx.context
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.MediaItemListHeaderBinding
import com.worldturtlemedia.playground.photos.databinding.MediaItemListHeaderBinding.bind

data class MediaItemListHeader(
    private val dateString: String,
    private val anySelected: Boolean,
    private val totalItemCount: Int,
    private val onSelectToggle: () -> Unit
) : ViewBindingItem<MediaItemListHeaderBinding>() {

    override fun getLayout(): Int = R.layout.media_item_list_header

    override fun inflate(itemView: View): MediaItemListHeaderBinding = bind(itemView)

    override fun bind(viewBinding: MediaItemListHeaderBinding, position: Int) {
        with(viewBinding) {
            txtDate.text = dateString

            val itemCountText =
                if (anySelected) context.getString(R.string.mash_selection_deselect)
                else context.getString(R.string.mash_selection_select, totalItemCount)

            txtSelectToggle.text = itemCountText
            txtSelectToggle.setOnClickListener { onSelectToggle() }
        }
    }
}