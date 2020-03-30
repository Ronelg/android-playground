package com.worldturtlemedia.playground.photos.googlephotos.ui.list.items

import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem

data class VideoListItem(
    override val mediaItem: MediaItem,
    override val isSelected: Boolean
) : MediaItemListItem() {

    override val mediaTypeIconRes: Int = R.drawable.ic_video
}