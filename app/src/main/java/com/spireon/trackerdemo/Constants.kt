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

    const val MOVE_START = "MOVE_START"
    const val MOVING = "MOVING"
    const val STILL = "STILL"
    const val MOVE_STOP = "MOVE_STOP"
    const val FORCE_END_TRIP = "FORCE_END_TRIP"

    object ACTION {
        const val MAIN_ACTION = "com.spireon.trackerdemo.action.main"
        const val START_TRACKING_ACTION = "com.spireon.trackerdemo.action.start"
        const val STOP_TRACKING_ACTION = "com.spireon.trackerdemo.action.stop"
        const val START_SERVICE_ACTION = "com.spireon.trackerdemo.action.startservice"
        const val STOP_SERVICE_ACTION = "com.spireon.trackerdemo.action.stopservice"
    }

    object NOTIFICATION_ID {
        const val FOREGROUND_SERVICE = 101
    }
}