package com.worldturtlemedia.playground.photos.googlephotos.model.filter

import com.google.photos.library.v1.proto.Filters
import com.google.photos.library.v1.proto.MediaTypeFilter

sealed class MediaFilter(private val value: MediaTypeFilter.MediaType) : Filter {

    object Video : MediaFilter(MediaTypeFilter.MediaType.VIDEO)
    object Photo : MediaFilter(MediaTypeFilter.MediaType.PHOTO)

    override fun build(builder: Filters.Builder): Filters.Builder {
        val filter = MediaTypeFilter.newBuilder().addMediaTypes(value).build()
        return builder.setMediaTypeFilter(filter)
    }
}