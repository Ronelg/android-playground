package com.worldturtlemedia.playground.photos.googlephotos.data.db.mediaitem

import androidx.room.*

@Dao
interface MediaItemDao {

    @Query("SELECT * FROM video_items")
    suspend fun allVideoItems(): List<VideoItemEntity>

    @Query("SELECT * FROM photo_items")
    suspend fun allPhotoItems(): List<PhotoItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoItems(items: List<VideoItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotoItems(items: List<PhotoItemEntity>)

    @Delete
    suspend fun deleteVideoItems(items: List<VideoItemEntity>)

    @Delete
    suspend fun deletePhotoItems(item: List<PhotoItemEntity>)

    @Query("DELETE from video_items")
    suspend fun deleteAllVideoItems()

    @Query("DELETE from photo_items")
    suspend fun deleteAllPhotoItems()
}