package com.worldturtlemedia.playground.photos.list

import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.photos.list.view.MediaTypeFilter

class PhotosListModel : StateViewModel<PhotosListState>(PhotosListState()) {

    fun changeMediaType(type: MediaTypeFilter) = setState {
        copy(mediaTypeFilter = type)
    }
}

data class PhotosListState(
    val mediaTypeFilter: MediaTypeFilter = MediaTypeFilter.All
) : State