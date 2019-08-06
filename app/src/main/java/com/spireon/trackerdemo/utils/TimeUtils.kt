package com.spireon.trackerdemo.utils

import android.util.Log
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Shubham Singh on 5/8/19.
 */

object TimeUtils {

    fun getStartTime():Date {
        val c = GregorianCalendar()
//        c.set(Calendar.DAY_OF_MONTH,5)
        c.set(Calendar.HOUR_OF_DAY, 0) //anything 0 - 23
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        Log.i("Timeutils",c.time.toString())
        return c.time
    }

    fun getEndTime(): Date {
        val c = GregorianCalendar()
//        c.set(Calendar.DAY_OF_MONTH,5)
        c.set(Calendar.HOUR_OF_DAY, 23) //anything 0 - 23
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        Log.i("Timeutils",c.time.toString())
        return c.time
    }

    fun getDuration(start: Date, end: Date): String {
       val diff = end.time - start.time
        val seconds = diff / 1000

        val day: Int = TimeUnit.SECONDS.toDays(seconds).toInt()
        val hours = TimeUnit.SECONDS.toHours(seconds) - day * 24
        val minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60
        val second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60

        var time = ""

        if(day>0) {
            time+= "${day}d "
        }

        if(hours>0) {
            time+= "${hours}h "
        }

        if(minute>0) {
            time+= "${minute}m"
        }

        if(second>0) {
            time+= "${second}s"
        }
        return time
    }
}