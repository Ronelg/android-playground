package com.worldturtlemedia.playground.photos.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.worldturtlemedia.playground.common.di.FakeDI
import com.worldturtlemedia.playground.photos.db.PhotosDatabase.Factory.DB_VERSION
import com.worldturtlemedia.playground.photos.googlephotos.data.db.GooglePhotosConverters
import com.worldturtlemedia.playground.photos.googlephotos.data.db.album.AlbumDao
import com.worldturtlemedia.playground.photos.googlephotos.data.db.album.AlbumEntity
import com.worldturtlemedia.playground.photos.googlephotos.data.db.mediaitem.MediaItemDao
import com.worldturtlemedia.playground.photos.googlephotos.data.db.mediaitem.PhotoItemEntity
import com.worldturtlemedia.playground.photos.googlephotos.data.db.mediaitem.VideoItemEntity

@Database(
    entities = [
        AlbumEntity::class,
        VideoItemEntity::class,
        PhotoItemEntity::class
    ],
    version = DB_VERSION
)
@TypeConverters(Converters::class, GooglePhotosConverters::class)
abstract class PhotosDatabase : RoomDatabase() {

    abstract fun albumDao(): AlbumDao

    abstract fun mediaItemDao(): MediaItemDao

    companion object Factory {

        const val DB_VERSION = 2
        const val DB_NAME = "photos_database"

        @Volatile
        private var instance: PhotosDatabase? = null

        fun getInstance(
            context: Context = FakeDI.applicationContext
        ) = instance ?: synchronized(this) {
            instance ?: create(context).also { instance = it }
        }

        private fun create(context: Context): PhotosDatabase =
            Room.databaseBuilder(context, PhotosDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}