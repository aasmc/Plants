package ru.aasmc.plants.data.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.aasmc.plants.data.model.Plant

@Database(entities = [Plant::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context)
                    .also {
                        INSTANCE = it
                    }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
        }
    }
}

private const val DATABASE_NAME = "sunflower_db"