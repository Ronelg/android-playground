package com.worldturtlemedia.playground.photos.googlephotos.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.worldturtlemedia.playground.common.core.Mapper
import com.worldturtlemedia.playground.photos.googlephotos.data.db.AlbumEntity.Companion.TABLE_NAME
import com.worldturtlemedia.playground.photos.googlephotos.model.Album

@Entity(tableName = TABLE_NAME)
data class AlbumEntity(
    @PrimaryKey val id: String,

    @ColumnInfo(name = "title") val title: String,

    @ColumnInfo(name = "product_url") val productUrl: String,

    @ColumnInfo(name = "item_count") val itemCount: Long,

    @ColumnInfo(name = "cover_photo_url") val coverPhotoUrl: String
) : Mapper<Album> {

    companion object {

        const val TABLE_NAME = "albums"
    }

    override fun map() = Album(
        id = id,
        title = title,
        productUrl = productUrl,
        itemCount = itemCount,
        coverPhotoUrl = coverPhotoUrl
    )
}

fun List<AlbumEntity>.mapToModel() = map { entity -> entity.map() }

fun Album.toEntity() = AlbumEntity(
    id = id,
    title = title,
    productUrl = productUrl,
    itemCount = itemCount,
    coverPhotoUrl = coverPhotoUrl
)

fun List<Album>.toEntities() = map { album -> album.toEntity() }