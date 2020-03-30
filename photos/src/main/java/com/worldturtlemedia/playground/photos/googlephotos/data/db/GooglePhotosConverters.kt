package com.worldturtlemedia.playground.photos.googlephotos.data.db

import androidx.room.TypeConverter
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.Dimensions

object GooglePhotosConverters {

    @JvmStatic
    @TypeConverter
    fun dimensionsToString(dimensions: Dimensions): String =
        "${dimensions.width},${dimensions.height}"

    @JvmStatic
    @TypeConverter
    fun stringToDimensions(dimensionsString: String): Dimensions {
        val (width, height) = dimensionsString.split(",").map { it.toLongOrNull() }
        if (width == null || height == null)
            throw IllegalArgumentException("Dimension String wasn't saved properly: $dimensionsString")

        return Dimensions(width = width, height = height)
    }
}