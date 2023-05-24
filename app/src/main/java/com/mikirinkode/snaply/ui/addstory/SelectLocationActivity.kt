package com.mikirinkode.snaply.ui.addstory

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.databinding.ActivitySelectLocationBinding
import com.mikirinkode.snaply.utils.PermissionManager
import com.mikirinkode.snaply.utils.Preferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    @Inject
    lateinit var preferences: Preferences

    private val binding: ActivitySelectLocationBinding by lazy {
        ActivitySelectLocationBinding.inflate(layoutInflater)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mMap: GoogleMap

    private var currentSelectedLocation: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        onActionClicked()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
        try {
            // check preference for dark mode
            val isDark = preferences.getBooleanValues(Preferences.DARK_MODE_PREF)
            if (isDark) {
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_night)
                )
            } else {
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_light)
                )
            }
        } catch (e: Exception) {
            Log.e("SelectLocation", "${e.message}")
        }

        val initialLocation = LatLng(-4.375726916664182, 117.53723749844212)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialLocation))

        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = true

        observeSavedLocation()
    }

    override fun onMapClick(latLng: LatLng) {
        if (currentSelectedLocation != null) {
            currentSelectedLocation?.remove()
        }
        val marker: Marker? = mMap.addMarker(createMarker(latLng))
        currentSelectedLocation = marker

        updateButton()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        navigateToLocation(marker.position)
        return true
    }

    private fun createMarker(latLng: LatLng): MarkerOptions {
        val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_custom_marker)
        return MarkerOptions().position(latLng).title("Your Selected Location").icon(markerIcon)
    }

    private fun observeSavedLocation() {

        val latitude = preferences.getStringValues(Preferences.SAVED_LATITUDE)
        val longitude = preferences.getStringValues(Preferences.SAVED_LONGITUDE)

        if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()) {
            val marker: Marker? =
                mMap.addMarker(createMarker(LatLng(latitude.toDouble(), longitude.toDouble())))
            currentSelectedLocation = marker

            updateButton()
        }
    }

    private fun updateButton() {
        binding.apply {
            if (currentSelectedLocation != null) {
                btnSelectLocation.isEnabled = true
            }
        }
    }

    private fun navigateToLocation(latLng: LatLng, zoom: Float = 14.0f) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun createUserLocationMarker(latLng: LatLng) {
        if (currentSelectedLocation != null) {
            currentSelectedLocation?.remove()
        }

        val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_user_location_marker)

        val marker = MarkerOptions()
            .position(latLng)
            .icon(markerIcon)
            .title("Your Location")

        currentSelectedLocation = mMap.addMarker(marker)

        updateButton()
    }

    private fun onActionClicked() {
        binding.apply {
            btnBack.setOnClickListener { onBackPressed() }

            btnSelectLocation.setOnClickListener {
                val intent = Intent()
                val lat = currentSelectedLocation?.position?.latitude
                val long = currentSelectedLocation?.position?.longitude
                val address = "Your Address:"

                preferences.setValues(Preferences.SAVED_LATITUDE, lat.toString())
                preferences.setValues(Preferences.SAVED_LONGITUDE, long.toString())

                intent.putExtra(AddStoryActivity.INTENT_ADDRESS, address)
                intent.putExtra(AddStoryActivity.INTENT_LAT, lat)
                intent.putExtra(AddStoryActivity.INTENT_LONG, long)
                setResult(RESULT_OK, intent)
                finish()
            }

            fabGoToUserLcoation.setOnClickListener {
                // check the permission
                if (PermissionManager.isLocationPermissionGranted(this@SelectLocationActivity)) {

                    // check the location service
                    if (isLocationServiceEnabled(this@SelectLocationActivity)) {
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location: Location? ->
                                // Use the retrieved location to navigate on Google Maps
                                location?.let {
                                    val userLatLng = LatLng(it.latitude, it.longitude)
                                    createUserLocationMarker(userLatLng)
                                    navigateToLocation(userLatLng)
                                }
                            }
                            .addOnFailureListener { exception: Exception ->
                                // Handle location retrieval failure
                                Toast.makeText(
                                    this@SelectLocationActivity,
                                    "${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        showEnableLocationServiceDialog()
                        Toast.makeText(
                            this@SelectLocationActivity,
                            "Location Service is Disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    PermissionManager.requestLocationPermission(this@SelectLocationActivity)
                }
            }
        }
    }

    private fun showEnableLocationServiceDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(R.string.txt_dialog_title_enable_location))
        builder.setMessage(getString(R.string.txt_dialog_desc_enable_location_service))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.txt_dialog_btn_open_settings)) { _, _ ->
                // Open settings screen to enable location services
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun isLocationServiceEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // handle the request permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.LOCATION_REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                for (result in grantResults) {
                    if (result == RESULT_OK) {
                        Toast.makeText(
                            this,
                            "Permissions are Granted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}