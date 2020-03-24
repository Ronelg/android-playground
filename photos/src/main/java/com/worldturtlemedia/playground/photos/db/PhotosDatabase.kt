package com.worldturtlemedia.playground.photos.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.worldturtlemedia.playground.common.di.FakeDI
import com.worldturtlemedia.playground.photos.db.PhotosDatabase.Factory.DB_VERSION
import com.worldturtlemedia.playground.photos.googlephotos.data.db.AlbumDao
import com.worldturtlemedia.playground.photos.googlephotos.data.db.AlbumEntity

@Database(
    entities = [
        AlbumEntity::class
    ],
    version = DB_VERSION
)
abstract class PhotosDatabase : RoomDatabase() {

    abstract fun albumDao(): AlbumDao

    companion object Factory {

        const val DB_VERSION = 1
        const val DB_NAME = "photos_database"

        @Volatile
        private var instance: PhotosDatabase? = null

        fun getInstance(
            context: Context = FakeDI.applicationContext
        ) = instance ?: synchronized(this) {
            instance ?: create(context).also { instance = it }
        }

        private fun create(context: Context): PhotosDatabase = Room.databaseBuilder(
            context,
            PhotosDatabase::class.java,
            DB_NAME
        ).build()
    }
}