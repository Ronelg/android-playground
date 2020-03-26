package com.worldturtlemedia.playground.photos.googlephotos.ui.list

import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.MediaFilter

class PhotosListModel : StateViewModel<PhotosListState>(PhotosListState()) {

    fun changeMediaType(type: MediaFilter) = setState {
        copy(mediaFilter = type)
    }
}

data class PhotosListState(
    val mediaFilter: MediaFilter = MediaFilter.All
) : State