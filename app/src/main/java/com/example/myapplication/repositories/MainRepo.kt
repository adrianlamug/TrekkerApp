package com.example.myapplication.repositories

import com.example.myapplication.db.Trekk
import com.example.myapplication.db.TrekkDAO
import javax.inject.Inject

//Collects Data from all data sources
// functions for each database query so we can use it as getters
class MainRepo @Inject constructor(
    val trekkDao: TrekkDAO
){
    suspend fun insertTrekk(trekk: Trekk) = trekkDao.insertTrekk(trekk)

    suspend fun deleteTrekk(trekk: Trekk) = trekkDao.deleteTrekk(trekk)

    fun getAllTrekksSortedByDate() = trekkDao.getAllTrekksSortedByDate()
    fun getAllRunsSortedByDistance() = trekkDao.getAllTrekksSortedByDistance()
    fun getAllTrekksSortedByTimeInMillis() = trekkDao.getAllTrekksSortedByTimeInMillis()
    fun getAllTrekksSortedByAvgSpeed() = trekkDao.getAllTrekksSortedByAvgSpeed()

    fun getTotalAvgSpeed() = trekkDao.getTotalAvgSpeed()
    fun getTotalDistance() = trekkDao.getTotalDistance()
    fun getTotalTimeInMillis() = trekkDao.getTotalTimeInMillis()
}