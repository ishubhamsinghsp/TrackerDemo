package com.spireon.trackerdemo.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.spireon.trackerdemo.Constants
import com.spireon.trackerdemo.activities.HomeActivity
import com.spireon.trackerdemo.data.AppDatabase
import com.spireon.trackerdemo.data.EventRepository
import com.spireon.trackerdemo.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*

/**
 * Created by Shubham Singh on 8/8/19.
 */

class ForegroundTrackingService : Service() {

    private lateinit var pendingIntent: PendingIntent
    private lateinit var pendingStartIntent: PendingIntent
    private lateinit var pendingStopIntent: PendingIntent
    private lateinit var notification:NotificationCompat.Builder
    private lateinit var mNotificationManagerCompat: NotificationManagerCompat
    private lateinit var repository: EventRepository
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLastLocation: Location
    // boolean flag to toggle periodic location updates
    private var mRequestingLocationUpdates = true
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var mLastActivity: String
    private var oldLocation = LatLng(0.0,0.0)
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var isStarted:Boolean = false
    private var lastMovement = Date()

    override fun onCreate() {
        super.onCreate()

        // Check if Google services are available
        if (getServicesAvailable()) {
            initFusedLocationClient()
            createLocationRequest()
//            Toast.makeText(this, "Google Service Is Available!!", Toast.LENGTH_SHORT).show()
        } else {
            stopSelf()
        }

        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)

        locationCallback = object :LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for(location in locationResult.locations) {
                    mLastLocation = location
                    handleLocationUpdate()
                }
            }
        }


        broadcastReceiver = object :BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    if (it.action.equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                        val type = it.getIntExtra("type", -1)
                        val confidence = it.getIntExtra("confidence", 0)
                        handleUserActivity(type, confidence)
                    }
                }
            }

        }
    }

    private fun storeData() {
        if(::mLastLocation.isInitialized && ::mLastActivity.isInitialized)  {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (mLastActivity) {
                    Constants.MOVING -> {
                        if(isStarted) {
                            insert(
                                Event(Constants.MOVING, mLastLocation.latitude, mLastLocation.longitude, Date.from(
                                    Instant.now()))
                            )
                        } else {
                            insert(
                                Event(Constants.MOVE_START, mLastLocation.latitude, mLastLocation.longitude, Date.from(
                                    Instant.now()))
                            )
                            isStarted = true
                        }
                    }

                    Constants.STILL -> {
                        if(isStarted) {
                            if ((Date.from(Instant.now()).time - lastMovement.time >= 600000)) {
                                insert(
                                    Event(Constants.MOVE_STOP, mLastLocation.latitude, mLastLocation.longitude, Date.from(
                                        Instant.now()))
                                )
                                isStarted = false
                            } else {
                                insert(
                                    Event(Constants.MOVING, mLastLocation.latitude, mLastLocation.longitude, Date.from(
                                        Instant.now()))
                                )
                            }
                        }
                    }

                    Constants.FORCE_END_TRIP -> {
                        if(isStarted) {
                            insert(
                                Event(
                                    Constants.FORCE_END_TRIP,
                                    mLastLocation.latitude,
                                    mLastLocation.longitude,
                                    Date.from(Instant.now())
                                )
                            )
                            isStarted = false
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleUserActivity(type:Int, confidence:Int) {

        if(confidence > Constants.CONFIDENCE) {

            val name = getActivityName(type)

            when(type) {
                DetectedActivity.IN_VEHICLE,
                DetectedActivity.ON_BICYCLE,
                DetectedActivity.RUNNING,
                DetectedActivity.ON_FOOT,
                DetectedActivity.WALKING
                -> {
                    if(::mLastActivity.isInitialized) {
                        if (mLastActivity != Constants.MOVING) {
                            mLastActivity = Constants.MOVING
                            storeData()
                        }
                    } else {
                        mLastActivity = Constants.MOVING
                        storeData()
                    }
                }
                DetectedActivity.STILL,
                DetectedActivity.UNKNOWN -> {
                    if(::mLastActivity.isInitialized) {
                        if (mLastActivity != Constants.STILL) {
                            mLastActivity = Constants.STILL
                            storeData()
                        }
                    } else {
                        mLastActivity = Constants.STILL
                        storeData()
                    }
                }
            }

            notification.setContentTitle("Activity: $mLastActivity ($name)\n Confidence: $confidence")
            mNotificationManagerCompat.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification.build())
        }
    }

    //Method to display the location on UI
    @SuppressLint("MissingPermission")
    private fun handleLocationUpdate() {

        if(!::mLastLocation.isInitialized) {
            fusedLocationProviderClient.lastLocation.result?.let{
                mLastLocation = it
            }
        }

        if(LatLng(mLastLocation.latitude, mLastLocation.longitude) != oldLocation) {
            val latitude = mLastLocation.latitude
            val longitude = mLastLocation.longitude
            val loc = "Location: $latitude ,$longitude "

            notification.setContentText(loc)
            mNotificationManagerCompat.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification.build())

            storeData()

            oldLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
        }

    }

    fun insert(event: Event) = GlobalScope.launch(Dispatchers.IO) {
        repository.insert(event)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback, null)
    }

    private fun start() {
        startTracking()
        startLocationUpdates()
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
            IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY)
        )
    }

    private fun stop() {
        stopTracking()
        stopLocationUpdates()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    private fun startTracking() {

        val intent = Intent(this, BackgroundDetectedActivitiesService::class.java)
        startService(intent)
    }

    private fun stopTracking() {
        val intent = Intent(this, BackgroundDetectedActivitiesService::class.java)
        stopService(intent)
        mLastActivity = "FORCE_END_TRIP"
        storeData()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when {
            intent?.action == Constants.ACTION.START_TRACKING_ACTION -> {
                notification.setContentText("Trip Tracking on")

                mNotificationManagerCompat.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification.build())

                Toast.makeText(this, "Tracking started", Toast.LENGTH_SHORT).show()

                start()

            }
            intent?.action == Constants.ACTION.STOP_TRACKING_ACTION -> {

                notification.setContentTitle("")
                notification.setContentText("Trip Tracking off")
                mNotificationManagerCompat.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification.build())
                Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show()
                stop()

            }
            intent?.action == Constants.ACTION.MAIN_ACTION -> {

            }
            intent?.action == Constants.ACTION.START_SERVICE_ACTION -> {

                val notificationIntent = Intent(this, HomeActivity::class.java)
                notificationIntent.action = Constants.ACTION.MAIN_ACTION
                notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

                val startIntent = Intent(this, ForegroundTrackingService::class.java)
                startIntent.action = Constants.ACTION.START_TRACKING_ACTION
                pendingStartIntent = PendingIntent.getService(this, 0, startIntent, 0)

                val stopIntent = Intent(this, ForegroundTrackingService::class.java)
                stopIntent.action = Constants.ACTION.STOP_TRACKING_ACTION
                pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, 0)

                val channelId =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        createNotificationChannel("my_service", "My Background Service")
                    } else {
                        // If earlier version channel ID is not used
                        // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                        ""
                    }

                notification = NotificationCompat.Builder(this, channelId)
                    .setTicker("Tracker Demo")
                    .setContentText("Trip Tracking on")
                    .setSmallIcon(android.R.drawable.ic_menu_directions)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_media_play,"Start tracking", pendingStartIntent)
                    .addAction(android.R.drawable.ic_media_pause, "Stop tracking", pendingStopIntent)

                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification.build())

                mNotificationManagerCompat = NotificationManagerCompat.from(this)

                start()

            }
            intent?.action == Constants.ACTION.STOP_SERVICE_ACTION -> {
                stopForeground(true)
                stopSelf()
                Toast.makeText(this, "Tracking service Stopped", Toast.LENGTH_SHORT).show()
            }
        }


        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun initFusedLocationClient() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun getServicesAvailable(): Boolean {
        val api = GoogleApiAvailability.getInstance()
        val isAvailable = api.isGooglePlayServicesAvailable(this)
        when {
            isAvailable == ConnectionResult.SUCCESS -> return true
//            api.isUserResolvableError(isAvailable) -> {
//                val dialog = api.getErrorDialog(this, isAvailable, 0)
//                dialog.show()
//            }
            else -> Toast.makeText(this, "Cannot Connect To Play Services", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = Constants.UPDATE_INTERVAL
        mLocationRequest.fastestInterval = Constants.FATEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.smallestDisplacement = Constants.DISPLACEMENT
    }

    private fun getActivityName(type: Int):String {
        return when(type) {
            DetectedActivity.IN_VEHICLE -> "vehicle"
            DetectedActivity.ON_BICYCLE -> "cycle"
            DetectedActivity.RUNNING -> "running"
            DetectedActivity.ON_FOOT -> "foot"
            DetectedActivity.WALKING -> "walking"
            DetectedActivity.STILL -> "still"
            DetectedActivity.UNKNOWN -> "unknown"
            else -> "unknown $type"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }
}