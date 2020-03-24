package com.worldturtlemedia.playground.photos.googlephotos.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface AlbumDao {

    @Query("SELECT * from ${AlbumEntity.TABLE_NAME}")
    fun allAlbumsFlow(): Flow<List<AlbumEntity>>

    @Query("SELECT * from ${AlbumEntity.TABLE_NAME}")
    suspend fun allAlbums(): List<AlbumEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(albums: List<AlbumEntity>)

    @Delete
    suspend fun deleteAll(user: AlbumEntity)
}

fun AlbumDao.getAlbumsFlow() = allAlbumsFlow().map { list ->
    list.map { entity -> entity.map() }
}

suspend fun AlbumDao.getAlbums() = allAlbums().map { entity -> entity.map() }