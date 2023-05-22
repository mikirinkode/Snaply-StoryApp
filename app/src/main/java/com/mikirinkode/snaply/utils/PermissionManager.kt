package com.mikirinkode.snaply.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.mikirinkode.snaply.ui.main.home.HomeFragment

object PermissionManager {

    const val LOCATION_REQUEST_PERMISSION_CODE = 11111

    fun requestLocationPermission(activity: Activity) {
        // check the permissions
        val requestPermissions = mutableListOf<String>()
        if (!isLocationPermissionGranted(activity)) {
            // if permissions are not granted
            requestPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (requestPermissions.isNotEmpty()) {
            // request the permission
            ActivityCompat.requestPermissions(
                activity,
                requestPermissions.toTypedArray(),
                LOCATION_REQUEST_PERMISSION_CODE
            )
        }
    }

    fun isLocationPermissionGranted(context: Context): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation && coarseLocation
    }
}