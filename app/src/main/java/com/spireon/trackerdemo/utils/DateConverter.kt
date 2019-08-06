package com.spireon.trackerdemo.utils

import android.util.Log
import androidx.room.TypeConverter
import java.util.*


/**
 * Created by Shubham Singh on 31/7/19.
 */

object DateConverter {
    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): Date? {
        Log.i("Dateconverter", "called with $value")
        return if (value == null) {
            null
        } else {
            Log.i("DateConverter", Date(value).toString())
            Date(value)
        }
    }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: Date?): Long? {
        Log.i("DateConverter", date?.time.toString())
        return date?.time
    }
}