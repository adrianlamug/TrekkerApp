package com.example.myapplication.ui.fragments

import android.graphics.Color
import android.location.Location
import com.example.myapplication.other.Constants
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import timber.log.Timber
/*
instances of this class draw lines that are within the users view, and colour those lines correctly
 */
class CheckTrailDistance{
    var idsOfDrawnLines = mutableListOf<Int>()
    // keep a list of the ids that were drawn
    // avoids redrawing lines
    val safePathColour = Color.rgb(33, 105, 35)  // green
    val unSafePathColour = Color.rgb(189, 38, 38)  // red
    val mixedSafetyPathColor = Color.rgb(252, 146, 58)    // yellow/orange

    fun changeTrailColDepOnDistance(startPoint: LatLng, givenTrail: Polyline) {
        // give the trail the correct colour depending on the distance from the user
        var atLeastOnePointSafe = false
        var atLeastOnePointUnsafe = false
        // booleans to track safety of the trail
        val loc1 = Location("start")

        loc1.latitude = startPoint.latitude
        loc1.longitude = startPoint.longitude

        val loc2 = Location("dest") // the destination changes as loop through each of the coordinates
        for (pathPoint in givenTrail.points){
            // loop through each cood
            loc2.latitude = pathPoint.latitude
            loc2.longitude = pathPoint.longitude
            val distanceInMeters = loc1.distanceTo(loc2) // get distance metres from location to current cood in loop

            if (distanceInMeters <= Constants.TRAVEL_RESTRICTION_METRES){
                // so a point is within the distance
                atLeastOnePointSafe = true
            }
            else{
                atLeastOnePointUnsafe = true
                // so at least on point is outside the distance
            }
        }
        if (atLeastOnePointSafe){
            if (atLeastOnePointUnsafe){
                // if have at least one point safe and one point unsafe then the trail
                // is partially within the travel restriction and partially outside it
                givenTrail.color = mixedSafetyPathColor
            }
            else{
                // so all points are safe
                givenTrail.color = safePathColour
            }
        }
        else{
            // so no points are safe, the entire trail is outside the travel restriction
            givenTrail.color = unSafePathColour
        }
    }

    fun drawLineDepOnScreen(trailsConverted: MutableList<Pair<Int, ArrayList<LatLng>>>, googleMap: GoogleMap, myLocCork: LatLng) {
        // draw a polyLine for a trail if the trail is within the user's view
        val curScreen = googleMap.projection.visibleRegion.latLngBounds
        // northeast have max lat and long
        // southwest have min lat and long
        val maxLat = curScreen.northeast.latitude
        val maxLong = curScreen.northeast.longitude
        val minLat = curScreen.southwest.latitude
        val minLong = curScreen.southwest.longitude
        for (convertedTrail in trailsConverted){
            val firstCood = convertedTrail.second[0]
            // get first cood
            val lastCood = convertedTrail.second.last()
            // get last cood
            val middleCood = convertedTrail.second[convertedTrail.second.lastIndex/2]
            val trailID = convertedTrail.first
            // trail ID

            if (trailID !in idsOfDrawnLines){
                // make sure the line has not already been drawn
                if ((minLat <= firstCood.latitude && firstCood.latitude <= maxLat
                        && minLong <= firstCood.longitude && firstCood.longitude <= maxLong)
                        ||
                        (minLat <= middleCood.latitude && middleCood.latitude <= maxLat
                                && minLong <= middleCood.longitude && middleCood.longitude <= maxLong)
                        ||
                        (minLat <= lastCood.latitude && lastCood.latitude <= maxLat
                                && minLong <= lastCood.longitude && lastCood.longitude <= maxLong)
                        )
          // will draw the line if the first, last, or middle cood is within users view
                        {

                    val polyline1 = googleMap.addPolyline(
                            PolylineOptions().addAll(convertedTrail.second)
                                    .clickable(true).width(Constants.POLYLINE_WIDTH)

                    )
                    idsOfDrawnLines.add(trailID) // add the trail id to the list of trails drawn
                    polyline1.tag = trailID
                    // set the tag as the id, which is the first received value
                    changeTrailColDepOnDistance(myLocCork, polyline1) // colour the drawn trail correctly
                }
            }

        }
    }
}