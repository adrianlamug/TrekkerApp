package com.example.myapplication.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.myapplication.repositories.MainRepo
import javax.inject.Inject

// assisting viewmodel class for the statistics fragment
class StatisticsViewModel @ViewModelInject constructor(
    val mainRepo: MainRepo
): ViewModel(){

    val totalTrailTime = mainRepo.getTotalTimeInMillis()
    val totalDistance = mainRepo.getTotalDistance()
    val totalAvgSpeed = mainRepo.getTotalAvgSpeed()

    val runsSortedByDate = mainRepo.getAllTrekksSortedByDate()
}