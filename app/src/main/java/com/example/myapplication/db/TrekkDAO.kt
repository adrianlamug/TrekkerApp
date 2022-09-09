package com.example.myapplication.db

import androidx.lifecycle.LiveData
import androidx.room.*

// Queries to get data from the database to be used in statistics
@Dao
interface TrekkDAO {

    // inserts new custom trail
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrekk(trekk: Trekk)

    // deletes custom trail
    @Delete
    suspend fun deleteTrekk(trekk: Trekk)

    // sorts all custom trails by date
    @Query("SELECT * FROM trail_table ORDER BY timestamp DESC")
    fun getAllTrekksSortedByDate(): LiveData<List<Trekk>>

    // sorts all custom trails by Time in ms
    @Query("SELECT * FROM trail_table ORDER BY timeInMillis DESC")
    fun getAllTrekksSortedByTimeInMillis(): LiveData<List<Trekk>>

    // sorts all custom trails by average speed
    @Query("SELECT * FROM trail_table ORDER BY avgSpeedInKMH DESC")
    fun getAllTrekksSortedByAvgSpeed(): LiveData<List<Trekk>>

    // sorts all custom trails by distance travelled
    @Query("SELECT * FROM trail_table ORDER BY distanceInMeters DESC")
    fun getAllTrekksSortedByDistance(): LiveData<List<Trekk>>

    // adds up total time spent in trails
    @Query("SELECT SUM(timeInMillis) FROM trail_table ")
    fun getTotalTimeInMillis(): LiveData<Long>

    // adds up total distance travelled
    @Query("SELECT SUM(distanceInMeters) FROM trail_table")
    fun getTotalDistance(): LiveData<Int>

    // gets the average speed of custom trails
    @Query("SELECT AVG(avgSpeedInKMH) FROM trail_table")
    fun getTotalAvgSpeed(): LiveData<Float>

}