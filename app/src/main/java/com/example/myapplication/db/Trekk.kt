package com.example.myapplication.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey


// Database variables for the custom trails database
@Entity(tableName = "trail_table")
data class Trekk(
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0.0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

}