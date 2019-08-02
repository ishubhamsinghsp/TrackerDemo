package com.spireon.trackerdemo

/**
 * Created by Shubham Singh on 1/8/19.
 */

object Constants {
    // Location updates intervals
    const val UPDATE_INTERVAL:Long = 3000 // 3 sec
    const val FATEST_INTERVAL:Long = 3000 // 5 sec
    const val DISPLACEMENT:Float = 10f // 10 meters

    const val BROADCAST_DETECTED_ACTIVITY = "activity_intent"
    const val DETECTION_INTERVAL_IN_MILLISECONDS:Long = 3000
    const val CONFIDENCE = 70
}