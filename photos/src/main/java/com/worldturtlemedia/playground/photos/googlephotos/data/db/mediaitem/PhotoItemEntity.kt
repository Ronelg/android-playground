package com.worldturtlemedia.playground.photos.googlephotos.data.db.mediaitem

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.PhotoItem
import org.joda.time.DateTime

@Entity(tableName = "photo_items")
data class PhotoItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "primary_key")
    override val primaryKey: Int,

    @Embedded
    override val item: PhotoItem,

    @ColumnInfo(name = "created_at")
    override val createdAt: Long
) : MediaItemEntity