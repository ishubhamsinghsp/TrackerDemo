package com.spireon.trackerdemo.model

import com.google.android.gms.maps.model.LatLng
import com.spireon.trackerdemo.utils.TimeUtils
import com.spireon.trackerdemo.utils.Utils
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Shubham Singh on 5/8/19.
 */
data class Trip(
    var events: ArrayList<Event>
) {
    fun tripStartLocation(): LatLng = LatLng(events[0].latitude, events[0].longitude)

    fun tripStartTime(): Date = events[0].time

    fun tripEndLocation(): LatLng = LatLng(events[events.size-1].latitude, events[events.size-1].longitude)

    fun tripEndTime(): Date = events[events.size-1].time

    fun tripDuration():String = TimeUtils.getDuration(events[0].time, events[events.size-1].time)

    fun tripDistance():Float = Utils.distance(tripStartLocation().latitude.toFloat(), tripStartLocation().longitude.toFloat(), tripEndLocation().latitude.toFloat(), tripEndLocation().longitude.toFloat())
}