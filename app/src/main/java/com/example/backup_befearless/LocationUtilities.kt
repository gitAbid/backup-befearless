package com.example.backup_befearless

import android.annotation.SuppressLint
import android.location.LocationManager
import java.util.*

class LocationUtilities {

    companion object {


        var locationManager: LocationManager? = null
        var latitude: String? = null
        var longitude: String? = null

        fun getMessage(
            locationManager: LocationManager,
            contactName: String,
            userName: String
        ): String {
            val branding = "- (Supported by Backup - Be Fearless)."
            this.locationManager = locationManager
            getLocation()
            val currentTime = Calendar.getInstance().getTime()
            var message =
                "Hi ${contactName}, this is ${userName}.This is an emergency. I need urgent help. My last known location at ${currentTime} is https://www.google.com/maps?q=${latitude},${longitude} .\n\n" + branding
            return message

        }

        @SuppressLint("MissingPermission")
        private fun getLocation() {

            //Check Permissions again


            val LocationGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val LocationNetwork =
                locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val LocationPassive =
                locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            if (LocationGps != null) {
                val lat = LocationGps.getLatitude()
                val longi = LocationGps.getLongitude()

                latitude = lat.toString()
                longitude = longi.toString()

            } else if (LocationNetwork != null) {
                val lat = LocationNetwork.getLatitude()
                val longi = LocationNetwork.getLongitude()

                latitude = lat.toString()
                longitude = longi.toString()

            } else if (LocationPassive != null) {
                val lat = LocationPassive.getLatitude()
                val longi = LocationPassive.getLongitude()

                latitude = lat.toString()
                longitude = longi.toString()

            }
        }

    }

}


