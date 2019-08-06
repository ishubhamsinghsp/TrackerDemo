package com.spireon.trackerdemo.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.spireon.trackerdemo.R
import com.spireon.trackerdemo.databinding.ActivityHomeBinding
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
            startActivity(Intent(this, MainActivity::class.java))
        }

        mBinding.btEvent.setOnClickListener {
            startActivity(Intent(this, EventListActivity::class.java))
        }

        mBinding.btTrips.setOnClickListener {
            startActivity(Intent(this, TripsListActivity::class.java))
        }
    }
}