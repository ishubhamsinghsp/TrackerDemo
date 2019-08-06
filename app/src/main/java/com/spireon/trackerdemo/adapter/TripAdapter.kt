package com.spireon.trackerdemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.spireon.trackerdemo.R
import com.spireon.trackerdemo.model.Trip

/**
 * Created by Shubham Singh on 5/8/19.
 */

class TripAdapter(private val trips: ArrayList<Trip>) : RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_trip, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tripDuration.text = "Duration: ${trips[position].tripDuration()}"
        holder.tripDistance.text = "Distance: ${trips[position].tripDistance()} m"
        holder.addressStart.text = "Address: ${trips[position].tripStartLocation()}"
        holder.addressEnd.text = "Address: ${trips[position].tripEndLocation()}"
        holder.timeStart.text = "Time: ${trips[position].tripStartTime()}"
        holder.timeEnd.text = "Time: ${trips[position].tripEndTime()}"
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tripDuration = itemView.findViewById<AppCompatTextView>(R.id.duration)
        val tripDistance = itemView.findViewById<AppCompatTextView>(R.id.distance)
        val addressStart = itemView.findViewById<AppCompatTextView>(R.id.address_start)
        val timeStart = itemView.findViewById<AppCompatTextView>(R.id.time_start)
        val addressEnd = itemView.findViewById<AppCompatTextView>(R.id.address_end)
        val timeEnd = itemView.findViewById<AppCompatTextView>(R.id.time_end)
    }

}