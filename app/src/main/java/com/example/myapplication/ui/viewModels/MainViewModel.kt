package com.example.myapplication.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.db.Trekk
import com.example.myapplication.other.SortType
import com.example.myapplication.repositories.MainRepo
import kotlinx.coroutines.launch
import javax.inject.Inject

// assisting viewmodel class for sorting custom trails while using queries from the database
class MainViewModel @ViewModelInject constructor(
    val mainRepo: MainRepo
): ViewModel(){

    private val trailsSortedByDate = mainRepo.getAllTrekksSortedByDate()
    private val trailsSortedByDistance = mainRepo.getAllRunsSortedByDistance()
    private val trailsSortedByTimeInMS = mainRepo.getAllTrekksSortedByTimeInMillis()
    private val trailsSortedByAvgSpeed = mainRepo.getAllTrekksSortedByAvgSpeed()

    val trails = MediatorLiveData<List<Trekk>>()

    var sortType = SortType.DATE

    init {
        trails.addSource(trailsSortedByDate) {result ->
            if(sortType == SortType.DATE) {
                result?.let { trails.value = it }
            }
        }
        trails.addSource(trailsSortedByDistance) {result ->
            if(sortType == SortType.DISTANCE) {
                result?.let { trails.value = it }
            }
        }
        trails.addSource(trailsSortedByTimeInMS) {result ->
            if(sortType == SortType.TRAIL_TIME) {
                result?.let { trails.value = it }
            }
        }
        trails.addSource(trailsSortedByAvgSpeed) {result ->
            if(sortType == SortType.AVG_SPEED) {
                result?.let { trails.value = it }
            }
        }
    }

    // sorts all the custom trails based on the filter selected
    fun sortTrails(sortType: SortType) = when(sortType){
        SortType.DATE -> trailsSortedByDate.value?.let{ trails.value = it}
        SortType.DISTANCE -> trailsSortedByDistance.value?.let{ trails.value = it}
        SortType.TRAIL_TIME -> trailsSortedByTimeInMS.value?.let{ trails.value = it}
        SortType.AVG_SPEED -> trailsSortedByAvgSpeed.value?.let{ trails.value = it}
    }.also {
        this.sortType = sortType
    }

    // inserts custom trail into the viewmodel
    fun insertTrail(trekk: Trekk) = viewModelScope.launch {
        mainRepo.insertTrekk(trekk)
    }
}