package com.example.myapplication.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.other.Constants
import com.example.myapplication.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

// Holds all dependencies for our tracking service and scoped to the lifetime of the service
@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    // one instance of the FusedLocationProviderClient for the lifetime of our service
    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
            @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)

    // service notification for getting user location on startup
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
            @ApplicationContext app: Context
    ) = PendingIntent.getActivity(
            app,
            0,
            Intent(app, MainActivity::class.java).also {
                it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
            },
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Notification to be found in the foreground service when tracking user location
    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
            @ApplicationContext app: Context,
            pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run_black)
            .setContentTitle("Trekker")
            .setContentText("00:00:00")
            .setContentIntent(pendingIntent)
}