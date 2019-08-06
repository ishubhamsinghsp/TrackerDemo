package com.spireon.trackerdemo.data

import androidx.annotation.WorkerThread
import com.spireon.trackerdemo.model.Event
import java.util.*

/**
 * Created by Shubham Singh on 31/7/19.
 */

class EventRepository(private val eventDao: EventDao) {

    @WorkerThread
    suspend fun insert(event: Event) {
        eventDao.insertAll(event)
    }

    @WorkerThread
    suspend fun deleteEvent(event: Event) {
        eventDao.delete(event)
    }

    @WorkerThread
    suspend fun getEventsByDate(startTime:Date, endTime:Date): List<Event> {
        return eventDao.getEventsByTime(startTime, endTime)
    }

    @WorkerThread
    suspend fun getAllEvents(): List<Event> {
        return eventDao.getAllEvents()
    }

    @WorkerThread
    suspend fun deleteAll() {
        eventDao.nukeTable()
    }
}