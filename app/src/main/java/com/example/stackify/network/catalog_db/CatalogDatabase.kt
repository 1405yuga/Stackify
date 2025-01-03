package com.example.stackify.network.catalog_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.stackify.entity.catalog.Catalog
import com.example.stackify.helper.Converters
import kotlin.concurrent.Volatile

@Database(entities = [Catalog::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CatalogDatabase : RoomDatabase() {

    abstract fun getCatalogDao(): CategoryDao

    companion object {
        @Volatile
        private var DATABASE_INSTANCE: CatalogDatabase? = null

        fun getCatalogDatabase(context: Context): CatalogDatabase {
            return DATABASE_INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(context, CatalogDatabase::class.java, "catalog_database")
                        .fallbackToDestructiveMigration()
                        .build()
                DATABASE_INSTANCE = instance
                instance
            }
        }
    }
}