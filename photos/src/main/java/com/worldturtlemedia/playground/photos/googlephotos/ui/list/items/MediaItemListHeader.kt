package com.worldturtlemedia.playground.photos.googlephotos.ui.list.items

import android.view.View
import com.worldturtlemedia.playground.common.base.ui.groupie.ViewBindingItem
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.MediaItemListHeaderBinding
import com.worldturtlemedia.playground.photos.databinding.MediaItemListHeaderBinding.bind
import org.joda.time.LocalDate

data class MediaItemListHeader(
    private val date: LocalDate
) : ViewBindingItem<MediaItemListHeaderBinding>() {

    companion object {
        const val MONTH_YEAR_PATTERN = "MMMM d, Y"
    }

    override fun getLayout(): Int = R.layout.media_item_list_header

    override fun inflate(itemView: View): MediaItemListHeaderBinding = bind(itemView)

    override fun bind(viewBinding: MediaItemListHeaderBinding, position: Int) {
        with(viewBinding) {
            txtDate.text = date.toString(MONTH_YEAR_PATTERN)
        }
    }

    override fun getId(): Long = date.toString().hashCode().toLong()
}