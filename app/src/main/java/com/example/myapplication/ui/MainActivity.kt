package com.example.myapplication.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.R
import com.example.myapplication.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.myapplication.other.Constants.ACTION_STARTUP_LOCATION_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

// Injects data into fragments so app doesn't crash
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var previousMagnitude = 0.0

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigateToTrackingFragment(intent)

        /*sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager*/
        navigateToTrekkFragment(intent)

        setSupportActionBar(toolbar)
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        navHostFragment.findNavController()
                .addOnDestinationChangedListener { _, destination, _ ->
                    when (destination.id) {
                        R.id.settingsFragment, R.id.trekkFragment, R.id.statisticsFragment, R.id.customTrailsFragment2 ->
                            bottomNavigationView.visibility = View.VISIBLE
                        else -> bottomNavigationView.visibility = View.GONE
                    }
                }

    }

    //If activity wasn't destroyed
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragment(intent)
        navigateToTrekkFragment(intent)
    }
    //Main Activity destroyed and our service is still running
    private fun navigateToTrackingFragment(intent: Intent?) {
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }

    // navigates to the main fragment
    private fun navigateToTrekkFragment(intent: Intent?) {
        if(intent?.action == ACTION_STARTUP_LOCATION_FRAGMENT) {
            navHostFragment.findNavController().navigate(R.id.second_action_global_trackingFragment)
        }
    }

}