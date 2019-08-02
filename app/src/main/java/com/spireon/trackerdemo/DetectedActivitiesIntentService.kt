package com.spireon.trackerdemo

import android.app.IntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityRecognitionResult



/**
 * Created by Shubham Singh on 2/8/19.
 */

class DetectedActivitiesIntentService :IntentService(DetectedActivitiesIntentService::class.java.simpleName) {
     val TAG = DetectedActivitiesIntentService::class.java.simpleName

    override fun onHandleIntent(intent: Intent?) {
        val result = ActivityRecognitionResult.extractResult(intent)

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        val detectedActivities = result.probableActivities as ArrayList

        for (activity in detectedActivities) {
            Log.e(TAG, "Detected activity: " + activity.type + ", " + activity.confidence)
            broadcastActivity(activity)
        }
    }

    private fun broadcastActivity(activity: DetectedActivity) {
        val intent = Intent(Constants.BROADCAST_DETECTED_ACTIVITY)
        intent.putExtra("type", activity.type)
        intent.putExtra("confidence", activity.confidence)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}