package com.example.myapplication.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.myapplication.R
import com.example.myapplication.other.Constants.ACTION_PAUSE_SERVICE
import com.example.myapplication.other.Constants.ACTION_STARTUP_LOCATION_FRAGMENT
import com.example.myapplication.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.myapplication.other.Constants.ACTION_STOP_SERVICE
import com.example.myapplication.other.Constants.ACTION_STOP_TRACK_SERVICE
import com.example.myapplication.other.Constants.ACTION_TRACK_SERVICE
import com.example.myapplication.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.myapplication.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.myapplication.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.myapplication.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.myapplication.other.Constants.NOTIFICATION_ID
import com.example.myapplication.other.Constants.NOTIFICATION_ID2
import com.example.myapplication.other.Constants.TIMER_INTERVAL
import com.example.myapplication.other.TrackingUtility
import com.example.myapplication.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingLocation : LifecycleService() {
    var firstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()

    //starting configuration for the notification
    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder

    // live data variables to be observed
    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val timeRunInMS = MutableLiveData<Long>()

        val startupLocationTracking = MutableLiveData<Boolean>()
        val startupPoints = MutableLiveData<LatLng>()
    }
    // initial values set
    private fun postInitialValues() {
        //Not tracking at the start
        startupLocationTracking.value = true
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        startupPoints.postValue(LatLng(0.0,0.0))
        timeRunInSeconds.postValue(0L)
        timeRunInMS.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        Timber.d("${isTracking.value}, ${startupLocationTracking.value}")
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        mFusedLocationProviderClient = FusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationWhenTracking(it)
        })
        startupLocationTracking.observe(this, Observer {
            updateLocationOnStartup(it)
        })
    }

    // kills the service
    private fun killService() {
        serviceKilled = true
        firstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    // when an action is triggered, it follows up with the services listed
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (firstRun) {
                        startForegroundService()
                        firstRun = false
                    } else {
                        Timber.d("Resuming service")
                        startTimer()
                    }
                    Timber.d("Started or resumed service")
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
                ACTION_TRACK_SERVICE -> {
                    Timber.d("Startup service started")
                    startForegroundStartupService()
                }
                ACTION_STOP_TRACK_SERVICE -> {
                    Timber.d("Stopped startup service")
                    stopStartUpTrackingService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //tracks actual time and triggers observers
    var timerEnabled = false
    private var currentTrailTime = 0L
    private var totalTrailTime = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    // starts timer
    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        timerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while(isTracking.value!!) {
                // Time difference between current time and time started
                currentTrailTime = System.currentTimeMillis() - timeStarted
                timeRunInMS.postValue(currentTrailTime + totalTrailTime)
                if(timeRunInMS.value!! >= lastSecondTimestamp+1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                //update in intervals
                delay(TIMER_INTERVAL)
            }
            totalTrailTime += currentTrailTime
        }
    }

    // pauses the custom trails tracking and stopwatch feature
    private fun pauseService() {
        isTracking.postValue(false)
        //pause timer
        timerEnabled=false
    }

    // stops the startup location tracking
    private fun stopStartUpTrackingService() {
        startupLocationTracking.postValue(false)
    }

    // returns location updates on intervals
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                        request, locationCallback, Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    // adds the locations to the addPathPoint function
    // returns the LatLng value of the user's location
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if(isTracking.value!!) {
                result?.locations?.let{locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("Location: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
            if(startupLocationTracking.value!!) {
                result?.locations?.let{locations ->
                    addLatLngLocation(locations.last())
                    Timber.d("PULL UP I SENT THE LOCATION: ${locations.last().latitude}, ${locations.last().longitude}")
                }
            }
        }
    }

    // adds LatLng values, to be used on startup of getting user location
    private fun addLatLngLocation(location: Location?){
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            startupPoints.postValue(pos)
        }
    }

    // adds current user position to be converted into a polyline
    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }

    //initialise empty polyline
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    // called when the user starts a custom trail
    // starts the timer and the tracking service
    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
        timeRunInSeconds.observe(this, Observer {
            if(!serviceKilled) {
                val notification = curNotificationBuilder
                        .setContentText(TrackingUtility.getStopWatchTime(it * 1000))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }


    // notification for the foreground service on startup
    private fun startForegroundStartupService() {
        startupLocationTracking.postValue(true)
        Timber.d("${ startupLocationTracking.value}")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_run_black)
                .setContentTitle("Trekker")
                .setContentIntent(getMainActivityPendingIntent())
        startForeground(NOTIFICATION_ID2, notificationBuilder.build())

    }
    // updates the notification with either pause or resume
    private fun updateNotificationWhenTracking(isTracking: Boolean) {
        val notificationActionText = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingLocation::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        }
        else {
            val resumeIntent = Intent(this, TrackingLocation::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // remove all actions before updating with new action
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if(!serviceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                    .addAction(R.drawable.ic_run_black, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }
    @SuppressLint("MissingPermission")
    private fun updateLocationOnStartup(startupLocationtracking: Boolean) {
        if (startupLocationtracking) {
            if(TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                    numUpdates = 2
                }
                mFusedLocationProviderClient.requestLocationUpdates(
                        request, locationCallback, Looper.getMainLooper()
                )
            }
        } else {
            mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }

    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).also {
                it.action = ACTION_STARTUP_LOCATION_FRAGMENT
            },
            FLAG_UPDATE_CURRENT
    )
    // creates the notification running in the foreground
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}