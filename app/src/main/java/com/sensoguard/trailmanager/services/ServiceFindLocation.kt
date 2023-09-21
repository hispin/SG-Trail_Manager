package com.sensoguard.trailmanager.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.global.CURRENT_LOCATION
import com.sensoguard.trailmanager.global.GET_CURRENT_LOCATION_KEY

class ServiceFindLocation :Service(){
    private val TAG="ServiceFindLocation"
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            run {
                Log.d(TAG, "get location")
                if (locationResult.lastLocation != null) {
                    location = locationResult.lastLocation!!
                    var inn = Intent(GET_CURRENT_LOCATION_KEY)
                    inn.putExtra(CURRENT_LOCATION, location)
                    sendBroadcast(inn)
                }
            }
        }
    }
    lateinit var location: Location


    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented")
    }


    override fun onCreate() {
        super.onCreate()
        startSysForeGround()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startSysForeGround()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest =
            LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000)
                .setFastestInterval(10000)//.setNumUpdates(1)
        startGetLocation()

        return START_NOT_STICKY
    }

    private fun startGetLocation(){
        try {
            if (ContextCompat.checkSelfPermission(
                    this.applicationContext,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationProviderClient.requestLocationUpdates(
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

    //The system allows apps to call Context.startForegroundService() even while the app is in the background. However, the app must call that service's startForeground() method within five seconds after the service is created
    private fun startSysForeGround() {
        fun getNotificationIcon(): Int {
            val useWhiteIcon =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            return if (useWhiteIcon) R.drawable.ic_app_notification else R.mipmap.ic_launcher
        }
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val `object` = getSystemService(Context.NOTIFICATION_SERVICE)
            if (`object` != null && `object` is NotificationManager) {
                `object`.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("SG-Trail Manager is running")
                .setSmallIcon(getNotificationIcon())
                .build()

            startForeground(1, notification)
        }
    }
}