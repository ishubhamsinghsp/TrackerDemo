package com.spireon.trackerdemo.activities

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.spireon.trackerdemo.Constants
import com.spireon.trackerdemo.R
import com.spireon.trackerdemo.databinding.ActivityHomeBinding
import com.spireon.trackerdemo.services.ForegroundTrackingService
import java.time.*
import java.time.ZoneId.getAvailableZoneIds
import java.time.ZoneId.systemDefault
import java.util.*



/**
 * Created by Shubham Singh on 2/8/19.
 */

class HomeActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        mBinding.btStart.setOnClickListener {
            val intent = Intent(this, ForegroundTrackingService::class.java)
            intent.action = Constants.ACTION.START_SERVICE_ACTION
            if(checkLocationPermissions()) {
                startService(intent)
            }
        }

        mBinding.btStop.setOnClickListener {
            val intent = Intent(this, ForegroundTrackingService::class.java)
            intent.action = Constants.ACTION.STOP_SERVICE_ACTION
            startService(intent)
        }

        mBinding.btStartMap.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        mBinding.btEvent.setOnClickListener {
            startActivity(Intent(this, EventListActivity::class.java))
        }

        mBinding.btTrips.setOnClickListener {
            startActivity(Intent(this, TripsListActivity::class.java))
        }
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
                        Toast.makeText(this@HomeActivity,"Permissions denied", Toast.LENGTH_SHORT).show()
                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // permission is denied permenantly, navigate user to app settings
                        isGranted = false
                        Toast.makeText(this@HomeActivity,"Enable permission from settings", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                    token.continuePermissionRequest()
                }
            })
            .withErrorListener { error ->
                isGranted = false
                Toast.makeText(this@HomeActivity,"Error: $error", Toast.LENGTH_SHORT).show()
            }
            .onSameThread()
            .check()
        return isGranted
    }
}