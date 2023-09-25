package com.example.myapplication.GeoCoding

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.io.IOException
import java.util.*

class GeocodingService(private val context: Context) {

    fun searchLocation(query: String): List<Address>? {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            return geocoder.getFromLocationName(query, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}
