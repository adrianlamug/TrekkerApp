package com.example.myapplication.ui.fragments

import android.content.ContentProviderClient
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.db.Trekk
import com.example.myapplication.other.Constants.ACTION_PAUSE_SERVICE
import com.example.myapplication.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.myapplication.other.Constants.ACTION_STOP_SERVICE
import com.example.myapplication.other.Constants.MAP_ZOOM
import com.example.myapplication.other.Constants.POLYLINE_COLOUR
import com.example.myapplication.other.Constants.POLYLINE_WIDTH
import com.example.myapplication.other.TrackingUtility
import com.example.myapplication.service.Polyline
import com.example.myapplication.service.Polylines
import com.example.myapplication.service.TrackingLocation
import com.example.myapplication.ui.viewModels.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null
    //Tracking
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var currentTimeInMS = 0L

    private var menu: Menu? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    // change visibility of menu item
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeInMS > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.cancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    // cancelling custom trail
    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Cancel Walk")
                .setMessage("Are you sure to cancel the current trail and delete all its data?")
                .setIcon(R.drawable.ic_cancel)
                .setPositiveButton("Yes") {_, _ ->
                    stopTrekk()
                }
                .setNegativeButton("No") {dialogInterface, _ ->
                    dialogInterface.cancel()
                }
                .create()
        dialog.show()
    }

    private fun stopTrekk() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_trekkFragment)
    }

    // Only showing the settings for this fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        btnToggleRun.setOnClickListener {
            toggleRun()
        }
        btnFinishRun.setOnClickListener{
            zoomToSeeWholeTrail()
            endTrailAndSaveToDB()
        }
        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        subscribeToObservers()
    }


    //connects all the polylines added
    private fun addAllPolylines() {
        for(polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                    .color(POLYLINE_COLOUR)
                    .width(POLYLINE_WIDTH)
                    .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    //adds latest gathered coordinates and draws it as polylines
    private fun addLatestPolyLine() {
        //if we have elements and at least 2 elements
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val previousLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                    .color(POLYLINE_COLOUR)
                    .width(POLYLINE_WIDTH)
                    .add(previousLastLatLng)
                    .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    // observes all live data changes
    private fun subscribeToObservers() {
        TrackingLocation.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingLocation.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyLine()
            moveCameraToUser()
        })

        TrackingLocation.timeRunInMS.observe(viewLifecycleOwner, Observer {
            currentTimeInMS = it
            val formattedTime = TrackingUtility.getStopWatchTime(currentTimeInMS, true)
            tvTimer.text = formattedTime
        })
    }

    // starts and stops the service
    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    // updates UI when tracking
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }

    //moves camera to center of user's location
    private fun moveCameraToUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            pathPoints.last().last(),
                            MAP_ZOOM
                    )
            )
        }
    }


    // sends the action to the tracking location service
    private fun sendCommandToService(action: String) = Intent(requireContext(), TrackingLocation::class.java).also {
        it.action = action
        requireContext().startService(it)
    }

    // after trail is finished, zooms out to see the whole trail
    private fun zoomToSeeWholeTrail(){
        val bounds = LatLngBounds.builder()
        for(polyline in pathPoints) {
            for(pos in polyline) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                        bounds.build(),
                        mapView.width,
                        mapView.height,
                        (mapView.height * 0.05f).toInt()
                )
        )
    }

    // ends the custom trail and saves the screenshot and the data for it
    private fun endTrailAndSaveToDB() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for(polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters/1000f) / (currentTimeInMS/1000f/60/60)*10)/10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
//            val caloriesBurned = ((distanceInMeters/1000f) * 80f).toInt()
            val trail = Trekk(bmp, dateTimestamp, avgSpeed, distanceInMeters,currentTimeInMS)
            viewModel.insertTrail(trail)
            Snackbar.make(
                    requireActivity().findViewById(R.id.rootView),
                    "Trail saved succesfully",
                    Snackbar.LENGTH_LONG
            ).show()
            stopTrekk()
        }
    }





    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}