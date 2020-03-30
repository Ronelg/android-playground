package com.worldturtlemedia.playground.photos.googlephotos.ui.list.items

import android.view.View
import androidx.annotation.DrawableRes
import coil.api.load
import com.worldturtlemedia.playground.common.base.ui.groupie.ViewBindingItem
import com.worldturtlemedia.playground.common.ktx.visibleOrGone
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.MediaItemListItemBinding
import com.worldturtlemedia.playground.photos.databinding.MediaItemListItemBinding.bind
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.Orientation

abstract class MediaItemListItem : ViewBindingItem<MediaItemListItemBinding>() {

    override fun getLayout(): Int = R.layout.media_item_list_item

    abstract val mediaItem: MediaItem
    abstract val isSelected: Boolean

    @get:DrawableRes
    abstract val mediaTypeIconRes: Int

    @get:DrawableRes
    private val orientationIconRes: Int by lazy {
        when (mediaItem.dimensions.orientation) {
            Orientation.Landscape -> R.drawable.ic_landscape_indicator
            Orientation.Portrait -> R.drawable.ic_portrait_indicator
            Orientation.Square -> R.drawable.ic_square_indicator
        }
    }

    override fun inflate(itemView: View): MediaItemListItemBinding = bind(itemView)

    override fun bind(viewBinding: MediaItemListItemBinding, position: Int) {
        with(viewBinding) {
            orientationIndicator.setImageResource(orientationIconRes)
            typeIndicator.setImageResource(mediaTypeIconRes)
            selectedOverlay.root.visibleOrGone = isSelected

            thumbnail.load(mediaItem.thumbnailUrl())
        }
    }

    override fun getId(): Long = mediaItem.id.hashCode().toLong()
}
