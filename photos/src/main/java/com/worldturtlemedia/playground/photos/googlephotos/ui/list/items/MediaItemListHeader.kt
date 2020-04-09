package com.worldturtlemedia.playground.photos.googlephotos.ui.list.items

import android.view.View
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.MediaItemListHeaderBinding
import com.worldturtlemedia.playground.photos.databinding.MediaItemListHeaderBinding.bind
import com.xwray.groupie.viewbinding.BindableItem
import org.joda.time.LocalDate

data class MediaItemListHeader(
    private val date: LocalDate
) : BindableItem<MediaItemListHeaderBinding>() {

    companion object {
        const val MONTH_YEAR_PATTERN = "MMMM d, Y"
    }

    override fun getLayout(): Int = R.layout.media_item_list_header

    override fun initializeViewBinding(view: View): MediaItemListHeaderBinding = bind(view)

    override fun bind(viewBinding: MediaItemListHeaderBinding, position: Int) {
        with(viewBinding) {
            txtDate.text = date.toString(MONTH_YEAR_PATTERN)
        }
    }

    override fun getId(): Long = date.toString().hashCode().toLong()
}