package com.mikirinkode.snaply.utils

import com.google.android.gms.maps.model.LatLng

// TODO: remove
class LinearLatLngInterpolator : LatLngInterpolator {
    override fun interpolate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
        val lat = (endValue.latitude - startValue.latitude) * fraction + startValue.latitude
        val lng = (endValue.longitude - startValue.longitude) * fraction + startValue.longitude
        return LatLng(lat, lng)
    }
}

interface LatLngInterpolator {
    fun interpolate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng
}