package com.example.myapplication.service

import android.graphics.Color
import com.example.myapplication.other.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import timber.log.Timber
import kotlin.properties.Delegates
/*
Instances of this class handle events when polylines are clicked on
 */
class PolyLineClickHandler {
    lateinit var currentMarker:Marker // keep track of the currently displaying marker so it can be removed when a new marker is clicked on
    lateinit var currentLine:com.google.android.gms.maps.model.Polyline // same as for the marker except for the line
    var polyLinePrevColour by Delegates.notNull<Int>() // need to remember the previous colour of the line so when another line is clicked on it can be returned back to its correct colour
    // Delegates.notNull<Int>() is the Int version of lateinit


    fun addMarkerMoveCamera(clickedLine: com.google.android.gms.maps.model.Polyline, googleMap: GoogleMap){

        removePrevious()
        // remove any previous marker and fix colour line when a new line is clicked on
        // debug line prints to console
        val linePoints = clickedLine.points
        // can use Timber or log to print out the id of the line when it is clicked on
        Timber.d(clickedLine.tag.toString())
        val firstPointOfPath = linePoints[0]
        // get first point of the clicked on path
        val newMarker = googleMap.addMarker((firstPointOfPath.let { MarkerOptions().position(firstPointOfPath).title("Trail Start") }))
        // add a marker to the start point of the map
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                firstPointOfPath,
                Constants.MAP_ZOOM
            )
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPointOfPath, 13.0F))
        // can move camera to first point of the path that was clicked on

        currentLine = clickedLine
        currentMarker = newMarker
        polyLinePrevColour = currentLine.color // save the colour so can be reverted back to
        currentLine.color = Color.BLACK // change the colour of the clicked on line to black

    }
    private fun removePrevious(){
        if (::currentMarker.isInitialized){
            // only perform these actions if the current Marker has been initialized
            // so only if there already exists a marker to delete
            currentMarker.remove()
            // remove the marker
            // set the colour back to the old colour
            currentLine.color = polyLinePrevColour
        }
    }
}