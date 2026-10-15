package com.sensoguard.trailmanager.fragments

import android.Manifest
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.adapters.CameraDialogAdapter
import com.sensoguard.trailmanager.classes.AlarmSensor
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.controler.ViewModelListener
import com.sensoguard.trailmanager.global.ALARM_CAR
import com.sensoguard.trailmanager.global.ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
import com.sensoguard.trailmanager.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.trailmanager.global.ALARM_INTRUDER
import com.sensoguard.trailmanager.global.ALARM_LOW_BATTERY
import com.sensoguard.trailmanager.global.ALARM_SENSOR_OFF
import com.sensoguard.trailmanager.global.CREATE_ALARM_ID_KEY
import com.sensoguard.trailmanager.global.CREATE_ALARM_KEY
import com.sensoguard.trailmanager.global.CREATE_ALARM_TYPE_KEY
import com.sensoguard.trailmanager.global.CURRENT_LATITUDE_PREF
import com.sensoguard.trailmanager.global.CURRENT_LOCATION
import com.sensoguard.trailmanager.global.CURRENT_LONGITUDE_PREF
import com.sensoguard.trailmanager.global.GET_CURRENT_LOCATION_KEY
import com.sensoguard.trailmanager.global.IS_NOTIFICATION_SOUND_KEY
import com.sensoguard.trailmanager.global.IS_VIBRATE_WHEN_ALARM_KEY
import com.sensoguard.trailmanager.global.MAP_SHOW_NORMAL_VALUE
import com.sensoguard.trailmanager.global.MAP_SHOW_SATELLITE_VALUE
import com.sensoguard.trailmanager.global.MAP_SHOW_VIEW_TYPE_KEY
import com.sensoguard.trailmanager.global.MAP_TYPE_KEY
import com.sensoguard.trailmanager.global.RESET_MARKERS_KEY
import com.sensoguard.trailmanager.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.trailmanager.global.UserSession
import com.sensoguard.trailmanager.global.convertBitmapToBitmapDescriptor
import com.sensoguard.trailmanager.global.getBooleanInPreference
import com.sensoguard.trailmanager.global.getCamerasFromLocally
import com.sensoguard.trailmanager.global.getIntInPreference
import com.sensoguard.trailmanager.global.getLongInPreference
import com.sensoguard.trailmanager.global.getStringInPreference
import com.sensoguard.trailmanager.global.setStringInPreference
import com.sensoguard.trailmanager.global.storeSensorsToLocally
import com.sensoguard.trailmanager.interfaces.OnAdapterListener
import com.sensoguard.trailmanager.services.ServiceFindLocation
import java.util.Calendar

private const val TAG = "MapSensorsFragment"

/**
 * The map of the cameras (long click on the map to locate a camera)
 */
class MapSensorsFragment : Fragment(), OnMapReadyCallback, OnAdapterListener {

    private var dialog: Dialog? = null
    private var sensorsDialogAdapter: CameraDialogAdapter? = null

    private var mMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null

    @Volatile
    private var myLocate: LatLng? = null
    private var currentLongitude: Double? = null
    private var currentLatitude: Double? = null
    private var fbRefresh: FloatingActionButton? = null
    private var fbChangeMapType: FloatingActionButton? = null
    private var mapType = GoogleMap.MAP_TYPE_SATELLITE
    private var ringtone: Ringtone? = null

    //the timer of the alarms (the alarm icon is shown for a while)
    private val timerViewModel: ViewModelListener
        get() = ViewModelProvider(this)[ViewModelListener::class.java]


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //start listener to timer
        startTimerListener()
    }

    //start listener to timer
    private fun startTimerListener() {
        timerViewModel.startCurrentCalendarListener()?.observe(this) {
            //if there is no alarm in process then shut down the timer
            if (UserSession.instance.alarmSensors.isNullOrEmpty()) {
                timerViewModel.shutDownTimer()
                stopPlayingAlarm()
                //refresh markers
                showMarkers()
            } else {
                //remove all the timed-out sensor alarms and show them with the regular sensor marker
                replaceSensorAlarmTimeOutToSensorMarker()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map_detects, container, false)

        fbRefresh = view.findViewById(R.id.fbRefresh)
        fbRefresh?.setOnClickListener {
            gotoMyLocation()
        }

        //toggle the type of the map
        fbChangeMapType = view.findViewById(R.id.fbChangeMapType)
        fbChangeMapType?.setOnClickListener {
            mapType = if (mMap?.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                GoogleMap.MAP_TYPE_SATELLITE
            } else {
                GoogleMap.MAP_TYPE_NORMAL
            }
            mMap?.mapType = mapType
        }

        return view
    }


    override fun onResume() {
        super.onResume()
        initMapType()

        //load the map
        if (isAdded) {
            val mapFragment = SupportMapFragment()
            mMapFragment = mapFragment
            childFragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit()
            mapFragment.getMapAsync(this)
        }

        //check if there is any sensor alarm that has not timed out, if yes then execute the timer
        if (isAnySensorAlarmNotTimeOut()) {
            startTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        timerViewModel.shutDownTimer()
        stopPlayingAlarm()
    }

    private fun stopPlayingAlarm() {
        if (ringtone?.isPlaying == true) {
            ringtone?.stop()
        }
    }

    //set the type of the map according to the configuration
    private fun initMapType() {
        when (getIntInPreference(activity, MAP_SHOW_VIEW_TYPE_KEY, -1)) {
            MAP_SHOW_NORMAL_VALUE -> mapType = GoogleMap.MAP_TYPE_NORMAL
            MAP_SHOW_SATELLITE_VALUE -> mapType = GoogleMap.MAP_TYPE_SATELLITE
        }

        //when the map is already loaded
        mMap?.mapType = mapType
    }

    //when the app start rotate ,then invoke this method and save the map type
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(MAP_TYPE_KEY, mapType)
    }

    //when the app complete rotate ,then invoke this method and restore the map type
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && savedInstanceState.containsKey(MAP_TYPE_KEY)) {
            mapType = savedInstanceState.getInt(MAP_TYPE_KEY)
            mMap?.mapType = mapType
        }
        super.onViewStateRestored(savedInstanceState)
    }

    //callback of location permission
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                startServiceFindLocation()
            }
        }

    private fun isLocationPermissionGranted(): Boolean {
        val context = context ?: return false
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun gotoMyLocation() {
        if (isLocationPermissionGranted()) {
            startServiceFindLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun startServiceFindLocation() {
        activity?.let {
            ContextCompat.startForegroundService(it, Intent(it, ServiceFindLocation::class.java))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        googleMap.clear()

        //set satellite as default
        googleMap.mapType = mapType

        //set the location of the current touching map
        googleMap.setOnMapLongClickListener { latLng ->
            currentLongitude = latLng.longitude
            currentLatitude = latLng.latitude
            showDialogSensorsList()
        }

        //go to last location
        val location = getLastKnownLocation()

        //set last location if exist
        location?.let { myLocate = LatLng(it.latitude, it.longitude) }

        showLocation(location)

        gotoMyLocation()
    }

    // move the camera to the location
    private fun showLocation(location: Location?) {

        if (location != null) {
            myLocate = LatLng(location.latitude, location.longitude)
        } else {
            //the last saved location, or default location (london) if there is no last location
            myLocate = getLastLocationLocally() ?: LatLng(51.509865, -0.118092)
        }
        //add marker at the focus of the map
        myLocate?.let {
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15.0f))
            showMarkers()
        }

    }

    //get last location from shared preference
    private fun getLastLocationLocally(): LatLng? {
        val lat = getStringInPreference(activity, CURRENT_LATITUDE_PREF, "-1")?.toDoubleOrNull()
        val lon = getStringInPreference(activity, CURRENT_LONGITUDE_PREF, "-1")?.toDoubleOrNull()
        if (lat == null || lon == null || lat == -1.0 || lon == -1.0) {
            return null
        }
        return LatLng(lat, lon)
    }

    //execute vibrate
    private fun playVibrate() {
        if (getBooleanInPreference(activity, IS_VIBRATE_WHEN_ALARM_KEY, true)) {
            // Vibrate for 1 second
            activity?.getSystemService(Vibrator::class.java)
                ?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    //start the timer that returns the sensors to the regular icon after the alarm time-out
    private fun startTimer() {
        timerViewModel.startTimer()
    }


    //show all markers
    private fun showMarkers() {

        mMap?.clear()

        //show current location marker
        showCurrentLocationMarker()

        //get sensors from locally
        val sensorsArr = activity?.let { getCamerasFromLocally(it) } ?: return

        for (sensorItem in sensorsArr) {
            if (sensorItem.getLatitude() == null || sensorItem.getLongitude() == null) {
                continue
            }

            val sensorAlarm = getSensorAlarmBySensor(sensorItem)
            if (sensorAlarm == null) {
                //show sensor marker
                showSensorMarker(sensorItem)
            } else if (isSensorAlarmTimeout(sensorAlarm)) {
                //if timed out then remove the sensor from the alarm list
                UserSession.instance.alarmSensors?.remove(sensorAlarm)
                showSensorMarker(sensorItem)
            } else {
                //save the marker for update after timeout
                sensorAlarm.marker = showSensorAlarmMarker(sensorItem, sensorAlarm.type)
            }
        }
    }

    //check if the alarm sensor is in duration
    private fun isSensorAlarmTimeout(alarmProcess: AlarmSensor): Boolean {
        val timeout = getLongInPreference(
            activity,
            ALARM_FLICKERING_DURATION_KEY,
            ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
        )
        val futureTimeout = alarmProcess.alarmTime.timeInMillis + timeout * 1000
        return futureTimeout < Calendar.getInstance().timeInMillis
    }

    //check if the sensor is in alarm process
    private fun getSensorAlarmBySensor(sensor: Camera): AlarmSensor? {
        return UserSession.instance.alarmSensors?.firstOrNull { it.alarmSensorId == sensor.getId() }
    }

    //the icon of a camera marker
    private fun markerIcon(resId: Int) = context?.let { convertBitmapToBitmapDescriptor(it, resId) }

    //show marker of sensor alarm
    private fun showSensorAlarmMarker(sensorItem: Camera, type: String): Marker? {
        val map = mMap ?: return null
        val latitude = sensorItem.getLatitude() ?: return null
        val longitude = sensorItem.getLongitude() ?: return null

        //set icon according to type alarm
        val alarmTypeIcon = when (type) {
            ALARM_CAR -> R.drawable.ic_alarm_car
            ALARM_INTRUDER -> R.drawable.ic_alarm_intruder
            ALARM_SENSOR_OFF -> R.drawable.ic_alarm_sensor_off
            ALARM_LOW_BATTERY -> R.drawable.ic_alarm_low_battery
            else -> R.drawable.ic_sensor_alarm
        }

        return map.addMarker(
            MarkerOptions()
                .position(LatLng(latitude, longitude))
                .draggable(true)
                .icon(markerIcon(alarmTypeIcon))
        )
    }

    //show marker of current location
    private fun showCurrentLocationMarker() {
        val map = mMap ?: return
        val location = myLocate ?: return
        map.addMarker(
            MarkerOptions()
                .position(location)
                .draggable(true)
                .icon(markerIcon(R.drawable.ic_my_locate))
        )
    }

    //show marker of sensor
    private fun showSensorMarker(sensorItem: Camera) {
        val map = mMap ?: return
        val latitude = sensorItem.getLatitude() ?: return
        val longitude = sensorItem.getLongitude() ?: return
        map.addMarker(
            MarkerOptions()
                .position(LatLng(latitude, longitude))
                .draggable(true)
                .icon(markerIcon(R.drawable.ic_sensor_item))
        )
    }

    //check if there is any sensor alarm that has not timed out
    private fun isAnySensorAlarmNotTimeOut(): Boolean {
        return UserSession.instance.alarmSensors?.any { !isSensorAlarmTimeout(it) } == true
    }


    //remove all the timed-out sensor alarms and show them with the regular sensor marker
    private fun replaceSensorAlarmTimeOutToSensorMarker() {
        val iteratorList = UserSession.instance.alarmSensors?.listIterator() ?: return
        while (iteratorList.hasNext()) {
            val sensorItem = iteratorList.next()
            if (isSensorAlarmTimeout(sensorItem)) {
                //show regular sensor marker
                sensorItem.marker?.setIcon(markerIcon(R.drawable.ic_sensor_item))
                //remove the sensor alarm because it timeout
                iteratorList.remove()
            }
        }
    }

    private fun playAlarmSound() {

        if (!getBooleanInPreference(activity, IS_NOTIFICATION_SOUND_KEY, true)) {
            return
        }

        val selectedSound = getStringInPreference(activity, SELECTED_NOTIFICATION_SOUND_KEY, "-1")

        if (selectedSound != null && selectedSound != "-1") {
            try {
                ringtone = RingtoneManager.getRingtone(activity, selectedSound.toUri())
                ringtone?.play()
            } catch (e: Exception) {
                Log.w(TAG, "play alarm sound failed", e)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(CREATE_ALARM_KEY)
        filter.addAction(RESET_MARKERS_KEY)
        filter.addAction(GET_CURRENT_LOCATION_KEY)
        activity?.let {
            ContextCompat.registerReceiver(
                it,
                alarmsReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(alarmsReceiver)
    }


    private fun showDialogSensorsList() {
        val context = context ?: return
        val sensors = getCamerasFromLocally(context) ?: return

        if (dialog?.isShowing == true) {
            sensorsDialogAdapter?.setDetects(sensors)
            return
        }

        val adapter = CameraDialogAdapter(sensors, context, this)
        sensorsDialogAdapter = adapter

        //create dialog
        val dialog = Dialog(context)
        this.dialog = dialog
        //set layout custom
        dialog.setContentView(R.layout.dialog_list_cameras)

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.75).toInt()
        dialog.window?.setLayout(width, height)

        val rvDetector = dialog.findViewById<RecyclerView>(R.id.rvSystemCameras)
        val btnSaveLocateSensor = dialog.findViewById<Button>(R.id.btnSaveLocateSensor)
        btnSaveLocateSensor?.setOnClickListener {
            CameraDialogAdapter.selectedSensor?.let { sensor ->
                currentLatitude?.let { sensor.setLatitude(it) }
                currentLongitude?.let { sensor.setLongitude(it) }
                saveLatLongDetector(sensor)
            }
            dialog.dismiss()
            showMarkers()
        }

        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(context, R.drawable.divider)
            ?.let { itemDecorator.setDrawable(it) }
        rvDetector?.addItemDecoration(itemDecorator)

        // Add some item here to show the list.
        rvDetector?.adapter = adapter
        rvDetector?.layoutManager = LinearLayoutManager(context)
        dialog.show()
    }

    private fun saveLatLongDetector(sensor: Camera) {
        val context = context ?: return
        val sensorsArr = getCamerasFromLocally(context) ?: return
        for (sensorItem in sensorsArr) {
            if (sensorItem.getId() == sensor.getId()) {
                sensor.getLatitude()?.let { sensorItem.setLatitude(it) }
                sensor.getLongitude()?.let { sensorItem.setLongitude(it) }
            }
        }
        storeSensorsToLocally(sensorsArr, context)
    }

    private val alarmsReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, inn: Intent) {
            when (inn.action) {
                //accept currentAlarm
                CREATE_ALARM_KEY -> {
                    val alarmSensorId = inn.getStringExtra(CREATE_ALARM_ID_KEY)
                    val type = inn.getStringExtra(CREATE_ALARM_TYPE_KEY)

                    //add alarm process to queue
                    if (alarmSensorId != null && type != null) {
                        UserSession.instance.alarmSensors?.add(
                            AlarmSensor(
                                alarmSensorId,
                                Calendar.getInstance(),
                                type
                            )
                        )
                    }

                    playAlarmSound()

                    playVibrate()

                    startTimer()

                    showMarkers()
                }

                GET_CURRENT_LOCATION_KEY -> {
                    val location =
                        IntentCompat.getParcelableExtra(inn, CURRENT_LOCATION, Location::class.java)
                    if (location != null) {
                        //save locally the current location
                        setStringInPreference(
                            activity,
                            CURRENT_LATITUDE_PREF,
                            location.latitude.toString()
                        )
                        setStringInPreference(
                            activity,
                            CURRENT_LONGITUDE_PREF,
                            location.longitude.toString()
                        )
                        showLocation(location)
                    } else {
                        Toast.makeText(activity, "error in location2", Toast.LENGTH_LONG).show()
                    }
                }

                RESET_MARKERS_KEY -> showMarkers()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //stop the GPS updates when the screen is closed
        activity?.stopService(Intent(activity, ServiceFindLocation::class.java))
    }

    //the last known location of the GPS (if the permission is granted)
    private fun getLastKnownLocation(): Location? {
        if (!isLocationPermissionGranted()) {
            return null
        }
        val locationManager = activity?.getSystemService(LocationManager::class.java) ?: return null
        return try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (ex: SecurityException) {
            Log.w(TAG, "no location permission", ex)
            null
        }
    }

    //rename the sensor name ,from the adapter
    override fun saveNameSensor(camera: Camera) {
        val context = context ?: return
        val detectorsArr = getCamerasFromLocally(context) ?: return
        for (detectorItem in detectorsArr) {
            if (detectorItem.getId() == camera.getId()) {
                camera.getName()?.let { detectorItem.setName(it) }
            }
        }
        storeSensorsToLocally(detectorsArr, context)
        showDialogSensorsList()
    }


}
