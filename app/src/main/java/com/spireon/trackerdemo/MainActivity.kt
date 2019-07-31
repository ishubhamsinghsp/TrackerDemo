package com.spireon.trackerdemo

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.spireon.trackerdemo.data.AppDatabase
import com.spireon.trackerdemo.data.EventRepository
import com.spireon.trackerdemo.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var repository: EventRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            insert(Event("START", 12.0, 33.1, Date.from(Instant.now())))
        }

    }

    fun insert(event: Event) = GlobalScope.launch(Dispatchers.IO) {
        repository.insert(event)
    }

    fun getAllEvents(): List<Event>? = runBlocking {
        repository.getAllEvents()
    }
}
