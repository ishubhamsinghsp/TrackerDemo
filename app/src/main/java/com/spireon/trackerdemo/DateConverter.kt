package com.spireon.trackerdemo

import androidx.room.TypeConverter
import java.util.*


/**
 * Created by Shubham Singh on 31/7/19.
 */

object DateConverter {
    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}