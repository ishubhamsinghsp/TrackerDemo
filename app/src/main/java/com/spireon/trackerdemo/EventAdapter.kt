package com.spireon.trackerdemo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.spireon.trackerdemo.model.Event

/**
 * Created by Shubham Singh on 2/8/19.
 */

class EventAdapter(private val events: List<Event>) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_event, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.eventTitle.text = "Event: ${events[position].event}"
        holder.locationTitle.text = "Location: ${events[position].latitude}, ${events[position].longitude}"
        holder.dateTitle.text = "Date: ${events[position].time}"
    }

    override fun getItemCount(): Int {
       return events.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val eventTitle = itemView.findViewById<AppCompatTextView>(R.id.tv_event)
        val locationTitle = itemView.findViewById<AppCompatTextView>(R.id.tv_location)
        val dateTitle = itemView.findViewById<AppCompatTextView>(R.id.tv_date)
    }

}