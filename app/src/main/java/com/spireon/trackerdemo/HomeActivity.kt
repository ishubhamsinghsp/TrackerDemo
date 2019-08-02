package com.spireon.trackerdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.spireon.trackerdemo.databinding.ActivityHomeBinding

/**
 * Created by Shubham Singh on 2/8/19.
 */

class HomeActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_home)

        mBinding.btStart.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        mBinding.btEvent.setOnClickListener {
            startActivity(Intent(this,EventListActivity::class.java))
        }
    }
}