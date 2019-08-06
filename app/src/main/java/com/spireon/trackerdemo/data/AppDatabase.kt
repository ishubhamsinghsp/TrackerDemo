package com.spireon.trackerdemo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.spireon.trackerdemo.model.Event
import com.spireon.trackerdemo.utils.DateConverter

/**
 * Created by Shubham Singh on 31/7/19.
 */

@Database(entities = [Event::class], version = 1)
@TypeConverters(
    DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile

        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if(tempInstance!=null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "TrackerDB"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}