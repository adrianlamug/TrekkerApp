package com.example.myapplication.other

import android.graphics.Color

// this file holds all the constants we use throughout our application
object Constants {
    const val RUNNING_DATABASE_NAME = "running_db"
    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"
    const val ACTION_STARTUP_LOCATION_FRAGMENT = "ACTION_STARTUP_LOCATION_FRAGMENT"
    const val ACTION_TRACK_SERVICE = "ACTION_TRACK_SERVICE"
    const val ACTION_STOP_TRACK_SERVICE = "ACTION_STOP_TRACK_SERVICE"

    const val TIMER_INTERVAL = 50L
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val SHARED_PREFERENCES = "sharedPreferences"
    const val KEY_FIRST_TOGGLE="KEY_FIRST_TOGGLE"
    const val KEY_NAME="KEY_NAME"

    const val POLYLINE_COLOUR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 17.5f

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1
    const val NOTIFICATION_ID2 = 2
    const val TRAVEL_RESTRICTION_METRES = 5000
}