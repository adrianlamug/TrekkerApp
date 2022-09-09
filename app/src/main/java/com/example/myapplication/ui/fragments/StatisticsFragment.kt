package com.example.myapplication.ui.fragments
//import androidx.fragment.app.viewModels
//import com.example.myapplication.ui.viewModels.StatisticsViewModel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.JobIntentService
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.myapplication.R
import com.example.myapplication.other.CustomBarChartView
//import com.example.myapplication.other.AlarmReceiver
import com.example.myapplication.other.TrackingUtility
import com.example.myapplication.ui.viewModels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlinx.android.synthetic.main.item_trekk.*
import kotlin.math.round


@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {
    private val viewModel: StatisticsViewModel by viewModels()
    /*step counter*/
//    private var running = false
//    private var sensorManager:SensorManager? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }
    /*alarm manager*/
//    @RequiresApi(Build.VERSION_CODES.N)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val datafetcher = 123
//        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        val c = Calendar.getInstance()
//        c.set(Calendar.HOUR_OF_DAY, 0)
//        c.set(Calendar.MINUTE, 0)
//        c.set(Calendar.SECOND, 0)
//
//        val alarmIntent = Intent(context, AlarmReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(context, datafetcher, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//        alarmManager.setInexactRepeating(AlarmManager.RTC, c.timeInMillis,AlarmManager.INTERVAL_DAY, pendingIntent)
//    }


//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        return inflater.inflate(R.layout.fragment_statistics, container, false)
//
//    }

//    @RequiresApi(Build.VERSION_CODES.KITKAT)
//    override fun onResume() {
//        super.onResume()
//        running = true
//        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//
//        if (stepsSensor == null) {
//            Toast.makeText(requireActivity(), "No Sensor Available", Toast.LENGTH_SHORT).show()
//        } else {
//            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
//        }
//    }

//    override fun onPause() {
//        super.onPause()
//        running = false
//        sensorManager?.unregisterListener(this)
//    }
//
//    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//    }
//
//    override fun onSensorChanged(event: SensorEvent) {
//        if (running) {
//            stepsValue.text = event.values[0].toString()
//        }
//    }

    // updates the data in the statistics with the live data being observed
    private fun subscribeToObservers(){
        viewModel.totalTrailTime.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTrailTime = TrackingUtility.getStopWatchTime(it)
                tvTotalTime.text = totalTrailTime
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) /10f
                val totalDistanceString = "${totalDistance}km"
                tvTotalDistance.text = totalDistanceString
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString ="${avgSpeed}"
                tvAverageSpeed.text = avgSpeedString
            }
        })

        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let{
                val totalAvgSpeeds = it.indices.map{ i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH)}
                val bardataSet = BarDataSet(totalAvgSpeeds, "Avg Speed Over Time").apply {
                    valueTextColor = Color.BLACK
                    color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                barChart.data = BarData(bardataSet)
                barChart.marker = CustomBarChartView(it.reversed(), requireContext(),R.layout.barchartview)
                barChart.invalidate()
            }
        })
    }

    // drawing the bar chart
    private fun setupBarChart() {
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.MAGENTA
            textColor = Color.BLACK
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = Color.MAGENTA
            textColor = Color.BLACK
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = Color.MAGENTA
            textColor = Color.BLACK
            setDrawGridLines(false)
        }
        barChart.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false
        }
    }
}

//class MyService : JobIntentService() {
//
//    @RequiresApi(Build.VERSION_CODES.N)
//    override fun onHandleWork(intent: Intent) {
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        val c = Calendar.getInstance()
//        c.set(Calendar.HOUR_OF_DAY, 0)
//        c.set(Calendar.MINUTE, 0)
//        c.set(Calendar.SECOND, 0)
//
//        val alarmIntent = Intent(this, AlarmReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0)
//        alarmManager.setInexactRepeating(AlarmManager.RTC, c.timeInMillis,AlarmManager.INTERVAL_DAY, pendingIntent)
//    }
//}

