package com.sensoguard.trailmanager.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager

import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sensoguard.trailmanager.global.CURRENT_LOCATION
import com.sensoguard.trailmanager.global.GET_CURRENT_LOCATION_KEY
import com.sensoguard.trailmanager.global.FOREGROUND_TYPE_LOCATION
import com.sensoguard.trailmanager.global.startForegroundWithNotification

private const val TAG = "ServiceFindLocation"

//get the current location (GPS) and broadcast it to the map screen
class ServiceFindLocation : Service() {
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d(TAG, "get location")
            locationResult.lastLocation?.let {
                val inn = Intent(GET_CURRENT_LOCATION_KEY)
                inn.setPackage(packageName)
                inn.putExtra(CURRENT_LOCATION, it)
                sendBroadcast(inn)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //since Android 14 a location foreground service can not start without location permission
        if (!isLocationPermissionGranted() || !startSysForeGround()) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            startGetLocation()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this.applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    //the permission is checked by isLocationPermissionGranted()
    @SuppressLint("MissingPermission")
    private fun startGetLocation() {
        try {
            if (isLocationPermissionGranted()) {
                val locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                        .setMinUpdateIntervalMillis(10000)
                        .build()
                fusedLocationProviderClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            Log.d("startGetLocation","exception:"+exception.message)
        }
    }

    private fun startSysForeGround(): Boolean {
        return try {
            startForegroundWithNotification(FOREGROUND_TYPE_LOCATION)
            true
        } catch (exception: SecurityException) {
            Log.d(TAG, "can not start foreground:" + exception.message)
            false
        }
    }
}
