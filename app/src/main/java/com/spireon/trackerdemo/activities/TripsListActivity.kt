package com.spireon.trackerdemo.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.spireon.trackerdemo.R
import com.spireon.trackerdemo.adapter.TripAdapter
import com.spireon.trackerdemo.data.AppDatabase
import com.spireon.trackerdemo.data.EventRepository
import com.spireon.trackerdemo.databinding.ActivityTripsBinding
import com.spireon.trackerdemo.model.Event
import com.spireon.trackerdemo.model.Trip
import com.spireon.trackerdemo.utils.TimeUtils
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Shubham Singh on 5/8/19.
 */

class TripsListActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityTripsBinding
    private lateinit var repository: EventRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_trips)

        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)

        mBinding.rvTrips.layoutManager = LinearLayoutManager(this)
        mBinding.rvTrips.setHasFixedSize(true)

        displayList()


    }

    private fun displayList() {
        val events = getEventsByTime()
        if(events.isNotEmpty()) {
            val tripList = calculateTrip(events)
            if(tripList.isNotEmpty()) {
                mBinding.rvTrips.adapter = TripAdapter(tripList)
                mBinding.rvTrips.visibility = View.VISIBLE
                mBinding.tvEmpty.visibility = View.GONE
            } else {
                mBinding.rvTrips.visibility = View.GONE
                mBinding.tvEmpty.visibility = View.VISIBLE
            }
        } else {
            mBinding.rvTrips.visibility = View.GONE
            mBinding.tvEmpty.visibility = View.VISIBLE
        }
    }

    private fun calculateTrip(events: List<Event>): ArrayList<Trip> {
        var isStarted = false
        val tripList = ArrayList<Trip>()
        var tripEventList = ArrayList<Event>()
        var lastMovement = Date()
        events.forEachIndexed { index, event ->
            when(event.event) {
                "MOVING" -> {
                    if(isStarted) {
                        tripEventList.add(event)
                    } else {
                        isStarted = true
                        tripEventList = ArrayList()
                        tripEventList.add(event)
                    }
                    lastMovement = event.time
                }
                "STILL" -> {
                    if(isStarted) {
                        if((events.size-1 == index) || (event.time.time - lastMovement.time >= 600000)) { //if its the last event or if its still for more than 10 mins end trip
                            isStarted = false
                            tripEventList.add(event)
                            tripList.add(Trip(tripEventList))
                        } else {
                            tripEventList.add(event)
                        }
                    }
                }
                "FORCE_END_TRIP" -> {
                    if(isStarted) {
                        isStarted = false
                        tripEventList.add(event)
                        tripList.add(Trip(tripEventList))
                    }
                }
            }
        }
        return tripList
    }

    private fun getEventsByTime(): List<Event> = runBlocking {
        repository.getEventsByDate(TimeUtils.getStartTime(), TimeUtils.getEndTime())
    }
}