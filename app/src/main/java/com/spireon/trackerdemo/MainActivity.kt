package com.spireon.trackerdemo

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.spireon.trackerdemo.data.AppDatabase
import com.spireon.trackerdemo.data.EventRepository
import com.spireon.trackerdemo.databinding.ActivityMainBinding
import com.spireon.trackerdemo.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import android.location.Criteria
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.time.Instant
import java.util.*
import kotlin.math.abs
import kotlin.math.sign

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
GoogleMap.OnInfoWindowClickListener,
GoogleApiClient.OnConnectionFailedListener{

    private lateinit var repository: EventRepository
    private lateinit var mMap: GoogleMap
    private lateinit var mBinding:ActivityMainBinding
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLastLocation: Location
    private val TAG = MainActivity::class.java.canonicalName
    private var markerCount: Int = 0
    // boolean flag to toggle periodic location updates
    private var mRequestingLocationUpdates = true
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var mLastActivity: String
    private var oldLocation = LatLng(0.0,0.0)
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Check if Google services are available
        if (getServicesAvailable()) {
            initFusedLocationClient()
            createLocationRequest()
//            Toast.makeText(this, "Google Service Is Available!!", Toast.LENGTH_SHORT).show()
        } else {
            finish()
        }

        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        BottomSheetBehavior.from(mBinding.llBottomInfo.bottomSheetLayout)

        locationCallback = object :LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for(location in locationResult.locations) {
                    mLastLocation = location
                    displayLocation()
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

        startTracking()

    }

    private fun initFusedLocationClient() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun storeData() {
        if(::mLastLocation.isInitialized && ::mLastActivity.isInitialized)  {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                insert(Event(mLastActivity, mLastLocation.latitude, mLastLocation.longitude, Date.from(Instant.now())))
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
                        if (mLastActivity != "MOVING") {
                            mLastActivity = "MOVING"
                            mBinding.llBottomInfo.tvActivity.text = "Activity: $mLastActivity ($name)"
                            storeData()
                        }
                    } else {
                        mLastActivity = "MOVING"
                        mBinding.llBottomInfo.tvActivity.text = "Activity: $mLastActivity ($name)"
                        storeData()
                    }
                }
                DetectedActivity.STILL,
                DetectedActivity.UNKNOWN -> {
                    if(::mLastActivity.isInitialized) {
                        if (mLastActivity != "STILL") {
                            mLastActivity = "STILL"
                            mBinding.llBottomInfo.tvActivity.text = "Activity: $mLastActivity ($name)"
                            storeData()
                        }
                    } else {
                        mLastActivity = "STILL"
                        mBinding.llBottomInfo.tvActivity.text = "Activity: $mLastActivity ($name)"
                        storeData()
                    }
                }
            }
            mBinding.llBottomInfo.tvConfidence.text = "Confidence: $confidence"
        }
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

    private fun getServicesAvailable(): Boolean {
        val api = GoogleApiAvailability.getInstance()
        val isAvailable = api.isGooglePlayServicesAvailable(this)
        when {
            isAvailable == ConnectionResult.SUCCESS -> return true
            api.isUserResolvableError(isAvailable) -> {
                val dialog = api.getErrorDialog(this, isAvailable, 0)
                dialog.show()
            }
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

    private fun checkLocationPermissions(): Boolean {
        var isGranted = false
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        isGranted = true
                    } else {
                        isGranted = false
                        Toast.makeText(this@MainActivity,"Permissions denied",Toast.LENGTH_SHORT).show()
                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // permission is denied permenantly, navigate user to app settings
                        isGranted = false
                        Toast.makeText(this@MainActivity,"Enable permission from settings",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                    token.continuePermissionRequest()
                }
            })
            .withErrorListener { error ->
                isGranted = false
                Toast.makeText(this@MainActivity,"Error: $error",Toast.LENGTH_SHORT).show()
            }
            .onSameThread()
            .check()
        return isGranted
    }

    fun insert(event: Event) = GlobalScope.launch(Dispatchers.IO) {
        repository.insert(event)
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            this.setShowWhenLocked(true)
            this.setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }

        getServicesAvailable()

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
            IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY)
        )

        // Resuming the periodic location updates
        if (mRequestingLocationUpdates) {
            startLocationUpdates()
            startTracking()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
//        if (mGoogleApiClient.isConnected) {
//            mGoogleApiClient.disconnect()
//        }
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermissions()
        mMap.setOnInfoWindowClickListener(this)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if(checkLocationPermissions()) {
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback, null)
        }
    }

    private fun startTracking() {

        val intent = Intent(this@MainActivity, BackgroundDetectedActivitiesService::class.java)
        startService(intent)
    }

    private fun stopTracking() {
        mLastActivity = "FORCE_END_TRIP"
        storeData()
        val intent = Intent(this@MainActivity, BackgroundDetectedActivitiesService::class.java)
        stopService(intent)
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        stopTracking()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.errorCode
        )
    }

    var mk:Marker? = null

    // Add A Map Pointer To The MAp
    private fun addMarker(googleMap:GoogleMap , lat:Double, lon:Double) {

        if(markerCount==1){
            mk?.let { animateMarker(mLastLocation, it) }
        }

        else if (markerCount==0){
            val smallMarker = getBitmapDescriptor(R.drawable.ic_car)
            mMap = googleMap

            val latlong = LatLng(lat, lon)
            mk= mMap.addMarker(MarkerOptions().position(LatLng(lat, lon))
                    .title("Me")
                    .icon(smallMarker)
                    .flat(true)
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 16f))

            //Set Marker Count to 1 after first marker is created
            markerCount=1

            if (!checkLocationPermissions()) {
                // TODO: Consider calling
                return
            }
            //mMap.setMyLocationEnabled(true);
            startLocationUpdates()
        }
    }

    private fun getBitmapDescriptor(id:Int):BitmapDescriptor {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
         val vectorDrawable: VectorDrawable =  ContextCompat.getDrawable(this, id) as VectorDrawable

        val h:Int = vectorDrawable.intrinsicHeight
        val w:Int = vectorDrawable.intrinsicWidth

        vectorDrawable.setBounds(0, 0, w, h)

        val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bm)

    } else {
            return BitmapDescriptorFactory.fromResource(id)
    }
}


    override fun onInfoWindowClick(marker: Marker) {
        Toast.makeText(this, marker.title, Toast.LENGTH_LONG).show()
    }

    //Method to display the location on UI
    @SuppressLint("MissingPermission")
    private fun displayLocation() {
        if(checkLocationPermissions()) {

            if(!::mLastLocation.isInitialized) {
                fusedLocationProviderClient.lastLocation.result?.let{
                    mLastLocation = it
                }
            }

            if(LatLng(mLastLocation.latitude, mLastLocation.longitude) != oldLocation) {
                val latitude = mLastLocation.latitude
                val longitude = mLastLocation.longitude
                val loc = "Location: $latitude ,$longitude "
                mBinding.llBottomInfo.tvLocation.text = loc
//            Toast.makeText(this, loc, Toast.LENGTH_SHORT).show()

                //Add pointer to the map at location
                addMarker(mMap, latitude, longitude)

                //Move map if marker is out of visible window
                if (!mMap.projection
                        .visibleRegion
                        .latLngBounds
                        .contains(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                ) {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLng(
                            LatLng(
                                mLastLocation.latitude,
                                mLastLocation.longitude
                            )
                        )
                    )
                }

                storeData()

                oldLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            }


        } else {
            Toast.makeText(this, "Couldn't get the location. Make sure location is enabled on the device",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
        val normalizeEnd = end - start // rotate start to 0
        val normalizedEndAbs = (normalizeEnd + 360) % 360

        val direction = (if (normalizedEndAbs > 180) -1 else 1).toFloat() // -1 = anticlockwise, 1 = clockwise
        val rotation: Float
        rotation = if (direction > 0) {
            normalizedEndAbs
        } else {
            normalizedEndAbs - 360
        }

        val result = fraction * rotation + start
        return (result + 360) % 360
    }

    private fun animateMarker(destination:Location, marker:Marker) {
        val startPosition: LatLng = marker.position
        val endPosition = LatLng(destination.latitude, destination.longitude)

        val startRotation: Float = marker.rotation

        val latLngInterpolator = LatLngInterpolator.LinearFixed()
        val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0F, 1F)
        valueAnimator.duration = 1000 // duration 1 second
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { animation ->
            try {
                val v = animation.animatedFraction
                val newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition)
                marker.position = newPosition
                marker.rotation = computeRotation(v, startRotation, destination.bearing)
            } catch (ex: Exception) {
                // I don't care atm..
            }
        }

        valueAnimator.start()
    }

    private interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng

        class LinearFixed : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                // Take the shortest path across the 180th meridian.
                if (abs(lngDelta) > 180) {
                    lngDelta -= sign(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }
}
