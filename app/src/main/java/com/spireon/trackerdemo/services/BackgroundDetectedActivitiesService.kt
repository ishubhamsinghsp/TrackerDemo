package com.spireon.trackerdemo.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.location.ActivityRecognitionClient
import android.app.PendingIntent
import android.os.Binder
import android.widget.Toast
import com.spireon.trackerdemo.Constants


/**
 * Created by Shubham Singh on 2/8/19.
 */

class BackgroundDetectedActivitiesService : Service() {

    private val TAG = BackgroundDetectedActivitiesService::class.java.simpleName

    private lateinit var mIntentService: Intent
    private lateinit var mPendingIntent: PendingIntent
    private lateinit var mActivityRecognitionClient: ActivityRecognitionClient

    var mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val serverInstance: BackgroundDetectedActivitiesService
            get() = this@BackgroundDetectedActivitiesService
    }

    override fun onCreate() {
        super.onCreate()
        mActivityRecognitionClient = ActivityRecognitionClient(this)
        mIntentService = Intent(this, DetectedActivitiesIntentService::class.java)
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT)
        requestActivityUpdatesHandler()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun requestActivityUpdatesHandler() {
        val task = mActivityRecognitionClient.requestActivityUpdates(
            Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
            mPendingIntent
        )

        task.addOnSuccessListener {
            Toast.makeText(
                applicationContext,
                "Successfully requested activity updates",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        task.addOnFailureListener {
            Toast.makeText(
                applicationContext,
                "Requesting activity updates failed to start",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun removeActivityUpdatesHandler() {
        val task = mActivityRecognitionClient.removeActivityUpdates(
            mPendingIntent
        )
        task.addOnSuccessListener {
            Toast.makeText(
                applicationContext,
                "Removed activity updates successfully!",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        task.addOnFailureListener {
            Toast.makeText(
                applicationContext, "Failed to remove activity updates!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeActivityUpdatesHandler()
    }

}