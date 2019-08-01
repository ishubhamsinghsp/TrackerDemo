package com.spireon.trackerdemo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.spireon.trackerdemo.model.Event

/**
 * Created by Shubham Singh on 31/7/19.
 */

@Dao
interface EventDao {
    @Query("SELECT * FROM event")
    suspend fun getAllEvents(): List<Event>

    @Query("SELECT * FROM event WHERE time BETWEEN :startTime AND :endTime")
    suspend fun getEventsByTime(startTime:Long, endTime:Long): List<Event>

    @Insert
    suspend fun insertAll(vararg events:Event)

    @Delete
    suspend fun delete(event: Event)
}