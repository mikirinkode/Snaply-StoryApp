package com.mikirinkode.snaply.ui.addstory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.databinding.ActivitySelectLocationBinding
import com.mikirinkode.snaply.utils.Preferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    @Inject
    lateinit var preferences: Preferences

    private val binding: ActivitySelectLocationBinding by lazy {
        ActivitySelectLocationBinding.inflate(layoutInflater)
    }

    private lateinit var mMap: GoogleMap

    private var currentSelectedLocation: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        onActionClicked()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
//        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)

        val initialLocation = LatLng(-4.375726916664182, 117.53723749844212)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialLocation))

        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = true

        observeSavedLocation()
    }

    override fun onMapClick(latLng: LatLng) {
        if (currentSelectedLocation != null){
            currentSelectedLocation?.remove()
        }
        val marker: Marker? = mMap.addMarker(createMarker(latLng))
        currentSelectedLocation = marker

        updateButton()
    }

    private fun createMarker(latLng: LatLng): MarkerOptions {
        return MarkerOptions().position(latLng).title("Your Selected Location")
    }

    private fun observeSavedLocation(){

        val latitude = preferences.getStringValues(Preferences.SAVED_LATITUDE)
        val longitude = preferences.getStringValues(Preferences.SAVED_LONGITUDE)

        if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()) {
            val marker: Marker? = mMap.addMarker(createMarker(LatLng(latitude.toDouble(), longitude.toDouble())))
            currentSelectedLocation = marker

            updateButton()
        }
    }

    private fun updateButton(){
        binding.apply {
            if (currentSelectedLocation != null){
                btnSelectLocation.isEnabled = true
            }
        }
    }

    private fun onActionClicked(){
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
        }
    }
}