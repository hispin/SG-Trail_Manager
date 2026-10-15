package com.sensoguard.trailmanager.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.Alarm
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.global.CREATE_ALARM_ID_KEY
import com.sensoguard.trailmanager.global.CREATE_ALARM_KEY
import com.sensoguard.trailmanager.global.CREATE_ALARM_NAME_KEY
import com.sensoguard.trailmanager.global.CREATE_ALARM_TYPE_KEY
import com.sensoguard.trailmanager.global.FOREGROUND_TYPE_DATA_SYNC
import com.sensoguard.trailmanager.global.READ_DATA_KEY
import com.sensoguard.trailmanager.global.RESET_MARKERS_KEY
import com.sensoguard.trailmanager.global.getAlarmsFromLocally
import com.sensoguard.trailmanager.global.getCamerasFromLocally
import com.sensoguard.trailmanager.global.startForegroundWithNotification
import com.sensoguard.trailmanager.global.storeAlarmsToLocally
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val TAG = "ServiceHandleAlarms"

//handle the alarms that arrive from the sensors (READ_DATA_KEY)
class ServiceHandleAlarms : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification(FOREGROUND_TYPE_DATA_SYNC)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ContextCompat.registerReceiver(
            this,
            alarmsReceiver,
            IntentFilter(READ_DATA_KEY),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(alarmsReceiver)
    }

    private fun refreshMarkers(context: Context, alarmSensorId: String) {
        sendBroadcast(Intent(RESET_MARKERS_KEY).setPackage(packageName))
        Toast.makeText(context, "Alarm from Unit $alarmSensorId", Toast.LENGTH_LONG).show()
    }

    private val alarmsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "accept alarm")
            //accept alarm
            if (intent.action != READ_DATA_KEY) {
                return
            }
            val bit = intent.getIntegerArrayListExtra("data") ?: return
            if (bit.size < 6) {
                return
            }

            val stateTypes = resources.getStringArray(R.array.state_types)
            val idx = bit[5].toUByte().toInt() - 1
            if (idx !in stateTypes.indices) {
                return
            }

            val type = stateTypes[idx]
            val alarmSensorId = bit[1].toUByte().toString()

            //get locally sensor that match to sensor of alarm
            val currentSensorLocally = getLocallySensorAlarm(alarmSensorId)

            //add alarm to history and send alarm if active
            when {
                currentSensorLocally == null -> {
                    refreshMarkers(context, alarmSensorId)
                    addNotActiveAlarmToHistory(false, "undefined", alarmSensorId, type)
                }

                // the sensor is not armed or the sensor id exist but is not located
                !currentSensorLocally.isArmed()
                        || currentSensorLocally.getLatitude() == null
                        || currentSensorLocally.getLongitude() == null -> {
                    refreshMarkers(context, alarmSensorId)
                    currentSensorLocally.getName()?.let {
                        addNotActiveAlarmToHistory(true, it, alarmSensorId, type)
                    }
                }

                else -> {
                    addAlarmToHistory(currentSensorLocally, type)

                    //send to create alarm :map,sound ect...
                    val inn = Intent(CREATE_ALARM_KEY)
                    inn.putExtra(CREATE_ALARM_ID_KEY, currentSensorLocally.getId())
                    inn.putExtra(CREATE_ALARM_NAME_KEY, currentSensorLocally.getName())
                    inn.putExtra(CREATE_ALARM_TYPE_KEY, type)
                    inn.setPackage(packageName)
                    sendBroadcast(inn)
                }
            }
        }
    }

    //the current date in the format of the alarm log
    private fun getCurrentDateString(calendar: Calendar): String {
        val locale = resources.configuration.locales.getFirstMatch(resources.assets.locales)
        return SimpleDateFormat(
            "kk:mm dd/MM/yy",
            locale ?: Locale.getDefault()
        ).format(calendar.time)
    }

    //add active alarm to history
    private fun addAlarmToHistory(currentSensorLocally: Camera, type: String) {
        val tmp = Calendar.getInstance()

        val alarm = Alarm(
            currentSensorLocally.getId(),
            currentSensorLocally.getName(),
            type,
            getCurrentDateString(tmp),
            currentSensorLocally.isArmed(),
            tmp.timeInMillis
        )
        alarm.latitude = currentSensorLocally.getLatitude()
        alarm.longitude = currentSensorLocally.getLongitude()
        alarm.isLocallyDefined = true

        addToStoredAlarms(alarm)
    }


    //add not active (not armed) alarm to history
    private fun addNotActiveAlarmToHistory(
        isLocallyDefined: Boolean,
        alarmSensorName: String,
        alarmSensorId: String,
        type: String
    ) {
        val tmp = Calendar.getInstance()
        val alarm = Alarm(
            alarmSensorId,
            alarmSensorName,
            type,
            getCurrentDateString(tmp),
            false,
            tmp.timeInMillis
        )
        alarm.isLocallyDefined = isLocallyDefined
        addToStoredAlarms(alarm)
    }

    private fun addToStoredAlarms(alarm: Alarm) {
        val alarms = getAlarmsFromLocally(this) ?: return
        alarms.add(alarm)
        storeAlarmsToLocally(alarms, this)
    }

    //get locally sensor that match to sensor of alarm
    private fun getLocallySensorAlarm(alarmSensorId: String): Camera? {
        return getCamerasFromLocally(this)?.firstOrNull { it.getId() == alarmSensorId }
    }
}
