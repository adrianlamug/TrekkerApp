package com.example.myapplication.ui.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.other.Constants
import com.example.myapplication.other.Constants.ACTION_STOP_TRACK_SERVICE
import com.example.myapplication.other.Constants.ACTION_TRACK_SERVICE
import com.example.myapplication.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.myapplication.other.TrackingUtility
import com.example.myapplication.service.PolyLineClickHandler
import com.example.myapplication.service.Polylines
import com.example.myapplication.service.TrackingLocation
import com.example.myapplication.ui.viewModels.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlinx.android.synthetic.main.fragment_trail.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

private const val KEY_CAMERA_POSITION = "camera_position"
private const val KEY_LOCATION = "location"
@AndroidEntryPoint
class TrekkFragment : Fragment(R.layout.fragment_trail), EasyPermissions.PermissionCallbacks,  OnMapReadyCallback, GoogleMap.OnPolylineClickListener {
    private val viewModel: MainViewModel by viewModels()
    private var trailsConverted = mutableListOf<Pair<Int, ArrayList<LatLng>>>()
    private var googleMap: GoogleMap? = null
    private var startupPoints = LatLng(51.893069, -8.500279)
    private var startupTrackingLocation = false
    private var trailsShown = false
    val trailChecker :CheckTrailDistance = CheckTrailDistance()
    val polyLineClickHandler = PolyLineClickHandler()

    // mix of code for fragment to activity and some taken from MapsFragment
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trail, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_trekkFragment_to_trackingFragment)
        }
        // added this from MapsFragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
//        mapFragment?.getMapAsync(callback)
        mapFragment?.getMapAsync{
            googleMap = it

        }

        if (startupPoints == LatLng(51.893069, -8.500279)){
            sendCommandToService(ACTION_TRACK_SERVICE)
            subscribeToObservers()
        }

//        if (savedInstanceState != null) {
//            startupPoints = savedInstanceState.getParcelable<LatLng>(KEY_LOCATION)!!
//            cameraPosition = savedInstanceState.getParcelable<CameraPosition>(KEY_CAMERA_POSITION)
//        }
        val trailparser = TrailParser()
        trailsConverted = trailparser.returnTrails(context as Context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs these permissions to run normally.",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs these permissions to run normally.",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }



    //For android versions like Q, permissions can be permanently denied. The app will crash if this case is not included
    //Leads the user to their settings and they can enable it from there.
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    //Not needed
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    //gets called whenever we request permissions, redirects to easypermissions library
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    // needed to override this function. Does not have any implementation

    override fun onMapReady(googleMap: GoogleMap) {

    }
    private fun sendCommandToService(action: String) = Intent(requireContext(), TrackingLocation::class.java).also {
        it.action = action
        requireContext().startService(it)
    }



    override fun onPolylineClick(p0: Polyline?) {
        // delegate polyline clicks to the polyLineClickHandler
        if (p0 != null) {
            googleMap?.let { polyLineClickHandler.addMarkerMoveCamera(p0, it) }
        }

    }
    private fun subscribeToObservers() {
        TrackingLocation.startupLocationTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
//            if (trailsShown == false) {
//
//            }
        })
        TrackingLocation.startupPoints.observe(viewLifecycleOwner, Observer {
            startupPoints = it
            Timber.d("SKRRRRT : ${startupPoints}")
            moveCameraToUser()
            // show trails and draw circle using current user position
            showTrails(it)
            drawTravelRestrCircle(it)
            // set an on click listener for all polylines
            googleMap?.setOnPolylineClickListener(this)
        })
    }
    private fun updateTracking(isTracking: Boolean) {
        this.startupTrackingLocation = isTracking
    }

    //moves camera to center of user's location
    private fun moveCameraToUser() {
        val pos = LatLng(startupPoints.latitude, startupPoints.longitude)
        if(startupPoints != null) {
            googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            pos,
                            Constants.MAP_ZOOM
                    )
            )
            googleMap?.addMarker(MarkerOptions().position(pos).title("Your Location"))
            //move map camera
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13.0F))



        }
    }
    private fun showTrails(userPos:LatLng) {

        /*
        trailsConverted has already been given a value in OnViewCreated
        trailsConverted can be passed in directly as a property
         */
        googleMap?.let { trailChecker.drawLineDepOnScreen(trailsConverted, it, userPos) }
        // a listener for when the camera is idle
        // will check position of the screen and draw visible lines
        googleMap?.setOnCameraIdleListener {

            trailChecker.drawLineDepOnScreen(trailsConverted, googleMap!!, userPos)

            // this function only draws lines within the users view
        }
    }
    private fun drawTravelRestrCircle(userPos: LatLng){
        // draw a circle surrounding the user showing the travel restrictions
        googleMap?.addCircle(CircleOptions()
                .center(userPos)
                .radius(Constants.TRAVEL_RESTRICTION_METRES.toDouble())
                .strokeColor(Color.RED))


    }



}