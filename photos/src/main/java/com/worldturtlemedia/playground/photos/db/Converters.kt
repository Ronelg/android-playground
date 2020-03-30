package com.worldturtlemedia.playground.photos.db

import androidx.room.TypeConverter
import org.joda.time.DateTime

object Converters {

    @JvmStatic
    @TypeConverter
    fun toDateTime(timestamp: Long): DateTime = DateTime(timestamp)

    @JvmStatic
    @TypeConverter
    fun fromDateTime(dateTime: DateTime): Long = dateTime.millis
}