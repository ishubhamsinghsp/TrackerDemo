package com.spireon.trackerdemo.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.spireon.trackerdemo.DateConverter
import java.util.Date

/**
 * Created by Shubham Singh on 31/7/19.
 */

@Entity
@TypeConverters(
    DateConverter::class)
data class Event(
    @ColumnInfo(name = "event") var event: String?,
    @ColumnInfo(name = "latitude") var latitude: Double?,
    @ColumnInfo(name = "longitude") var longitude: Double?,
    @ColumnInfo(name = "time") var time: Date
) {
    @PrimaryKey(autoGenerate = true) var eventId: Int = 0
}