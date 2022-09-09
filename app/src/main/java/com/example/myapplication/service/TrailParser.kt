package com.example.myapplication.ui.fragments

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import java.io.File
import java.io.InputStream
import java.io.InputStream

.*
/*
This class is used to parse the trails in the json a file into a pair
containing the trail id and its coordinates
 */
class TrailParser {
    fun returnTrails(c: Context): MutableList<Pair<Int, ArrayList<LatLng>>> {
        //read from assets
        val allTrailsStream = c.assets.open("allTrailsJSON.json")
        //use assets however
        // readFileText(c, "allTrailsJSON.json")
        val inputString = allTrailsStream.bufferedReader().use {
            it.readText()
        }
        val trailHolder = Gson().fromJson<TrailHolder>(inputString, TrailHolder::class.java)
        val allTrails = trailHolder.trails

        // val allTrailsCoodsAsLatLng: ArrayList<ArrayList<LatLng>> = arrayListOf()
        val idAndCoods = mutableListOf<Pair<Int,ArrayList<LatLng>>>()
        for (trail in allTrails) {
            val coordList: ArrayList<LatLng> = arrayListOf()

            // Adding points to ArrayList
            for (coodPair in trail.coods) {
                coordList.add(LatLng(coodPair[0], coodPair[1]))
            }

            idAndCoods.add(Pair(trail.id, coordList))
            // add
        }
        // first item is id second is arraylist of latlng coordinates
        return idAndCoods
    }

}
val inputStream: InputStream = File("app\\src\\main\\assets\\allTrailsJSON.json").inputStream()
val inputString = inputStream.bufferedReader().use {
    it.readText()
}
val trailHolder = Gson().fromJson<TrailHolder>(inputString, TrailHolder::class.java)

data class TrailHolder(
        // trail holder is just the single object with one attribute, a list of all path objects
        val trails: List<Trail>
)

data class Trail(
        val coods: List<List<Double>>,
        val id: Int,
        val length: Int,
        val numOfCoods: Int,
        val tags: List<String>
)

fun main() {
    // a simple test function
    // println(trailHolder)
    // able to iterate through them all trails
    for (trail in trailHolder.trails){
        val coordList: ArrayList<LatLng> = arrayListOf()

        // Adding points to ArrayList
        for (coodPair in trail.coods){
            coordList.add(LatLng(coodPair[0], coodPair[1]))
        }
        println(coordList)

    }
}



