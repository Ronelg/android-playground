package com.worldturtlemedia.playground.photos.googlephotos.model.filter

import androidx.annotation.StringRes
import com.google.photos.library.v1.proto.Filters
import com.google.photos.library.v1.proto.MediaTypeFilter
import com.worldturtlemedia.playground.photos.R

sealed class MediaFilter(private val value: MediaTypeFilter.MediaType?) : Filter {

    object All : MediaFilter(null)
    object Video : MediaFilter(MediaTypeFilter.MediaType.VIDEO)
    object Photo : MediaFilter(MediaTypeFilter.MediaType.PHOTO)

    override fun build(builder: Filters.Builder): Filters.Builder {
        return if (value == null) builder.clearMediaTypeFilter()
        else {
            val filter = MediaTypeFilter.newBuilder().addMediaTypes(value).build()
            builder.setMediaTypeFilter(filter)
        }
    }

    @get:StringRes
    val stringRes: Int
        get() = when (this) {
            All -> R.string.media_type_all
            Video -> R.string.media_type_videos
            Photo -> R.string.media_type_photos
        }
}