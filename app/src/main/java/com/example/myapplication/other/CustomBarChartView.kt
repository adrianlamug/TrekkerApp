package com.example.myapplication.other

import android.content.Context
import com.example.myapplication.db.Trekk
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.barchartview.view.*
import java.text.SimpleDateFormat
import java.util.*

// builds the stats for the bar selected in the bar chart
class CustomBarChartView(
    val trails: List<Trekk>,
    context: Context,
    layoutID: Int
): MarkerView(context, layoutID) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f, -height.toFloat())
    }
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e == null){
            return
        }
        val currentTrailID = e.x.toInt()
        val trail = trails[currentTrailID]
        val calendar = Calendar.getInstance().apply {
            timeInMillis = trail.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${trail.avgSpeedInKMH}km/h"
        tvAvgSpeed.text = avgSpeed

        val distanceInKM = "${trail.distanceInMeters/1000f}km"
        tvDistance.text = distanceInKM

        tvDuration.text = TrackingUtility.getStopWatchTime(trail.timeInMillis)
    }

}
