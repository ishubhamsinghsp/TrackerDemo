package com.spireon.trackerdemo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.spireon.trackerdemo.data.AppDatabase
import com.spireon.trackerdemo.data.EventRepository
import com.spireon.trackerdemo.databinding.ActivityEventListBinding
import com.spireon.trackerdemo.model.Event
import kotlinx.coroutines.runBlocking

/**
 * Created by Shubham Singh on 2/8/19.
 */

class EventListActivity :AppCompatActivity() {
    private lateinit var mBinding: ActivityEventListBinding
    private lateinit var repository: EventRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_event_list)

        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)

        mBinding.rvEvents.layoutManager = LinearLayoutManager(this)
        mBinding.rvEvents.setHasFixedSize(true)
        displayList()
    }

    private fun displayList() {
        val events = getAllEvents()
        if(events.isNotEmpty()) {
            mBinding.rvEvents.adapter = EventAdapter(events)
            mBinding.rvEvents.visibility = View.VISIBLE
            mBinding.tvEmpty.visibility = View.GONE

        } else {
            mBinding.rvEvents.visibility = View.GONE
            mBinding.tvEmpty.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_delete -> {
                runBlocking {
                    repository.deleteAll()
                }
                displayList()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getAllEvents(): List<Event> = runBlocking {
        repository.getAllEvents()
    }
}