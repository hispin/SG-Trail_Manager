package com.sensoguard.hunter.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.Alarm
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.global.*
import java.text.SimpleDateFormat
import java.util.*


class ServiceHandleAlarms : Service(){
        private val TAG="ServiceHandleAlarms"



        override fun onBind(intent: Intent?): IBinder? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }


        override fun onCreate() {
            super.onCreate()
            startSysForeGround()

        }



        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            //FusedLocationProviderClient is for interacting with the location using fused location
            setFilter()

            return START_NOT_STICKY
        }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    private fun setFilter() {
        val filter = IntentFilter(READ_DATA_KEY)
        registerReceiver(usbReceiver, filter)
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG,"accept alarm")
            //accept alarm
            if (intent.action == READ_DATA_KEY) {
                val bit=intent.getIntegerArrayListExtra("data")

                val stateTypes = resources?.getStringArray(R.array.state_types)

                val idx = bit[5].toUByte().toInt()-1

                if (stateTypes != null && idx >= stateTypes.size) {
                    return
                }

                val type=stateTypes?.get(bit[5].toUByte().toInt()-1)
                val alarmSensorId = bit[1].toUByte().toString()

                //get locally sensor that match to sensor of alarm
                val currentSensorLocally=getLocallySensorAlarm(alarmSensorId)

                //add alarm to history and send alarm if active
                if(currentSensorLocally==null){
                     sendBroadcast(Intent(RESET_MARKERS_KEY))
                     Toast.makeText(context, "Alarm from Unit $alarmSensorId", Toast.LENGTH_LONG).show()
                     addAlarmToHistory(false,"undefined", isArmed = false, alarmSensorId = alarmSensorId, type = type)
                }else if(!currentSensorLocally.isArmed()){
                     sendBroadcast(Intent(RESET_MARKERS_KEY))
                     Toast.makeText(context, "Alarm from Unit $alarmSensorId", Toast.LENGTH_LONG).show()
                    currentSensorLocally.getName()?.let {
                        addAlarmToHistory(true,
                            it, isArmed = false, alarmSensorId = alarmSensorId, type = type)
                    }
               // the sensor id exist but is not located
                }else if(currentSensorLocally.getLatitude()==null
                    || currentSensorLocally.getLongtitude()==null) {
                    sendBroadcast(Intent(RESET_MARKERS_KEY))
                    Toast.makeText(context, "Alarm from Unit $alarmSensorId", Toast.LENGTH_LONG).show()
                    currentSensorLocally.getName()?.let {
                        addAlarmToHistory(true,
                            it, isArmed = false, alarmSensorId = alarmSensorId, type = type)
                    }
                }else{
                    type?.let { addAlarmToHistory(currentSensorLocally, it) }

                    //send to create alarm :ic_map_main,sound ect...
                    val inn = Intent(CREATE_ALARM_KEY)
                    inn.putExtra(CREATE_ALARM_ID_KEY,currentSensorLocally.getId())
                    inn.putExtra(CREATE_ALARM_NAME_KEY,currentSensorLocally.getName())
                    //inn.putExtra(CREATE_ALARM_LATITUDE_KEY,currentSensorLocally.getLatitude())
                    //inn.putExtra(CREATE_ALARM_LONGTITUDE_KEY,currentSensorLocally.getLongtitude())
                    inn.putExtra(CREATE_ALARM_TYPE_KEY,type)
                    sendBroadcast(inn)
                }
            }
        }
    }

    //add active alarm to history
    private fun addAlarmToHistory(currentSensorLocally: Camera, type: String) {
        val tmp = Calendar.getInstance()
        val resources=this.resources
        val locale=if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) resources.configuration.locales.getFirstMatch(resources.assets.locales)
        else resources.configuration.locale
        val dateFormat= SimpleDateFormat("kk:mm dd/MM/yy", locale)
        val dateString=dateFormat.format(tmp.time)

        val alarm= Alarm(
            currentSensorLocally.getId(),
            currentSensorLocally.getName(),
            type,
            dateString,
            currentSensorLocally.isArmed(),
            tmp.timeInMillis
        )
        alarm.latitude=currentSensorLocally.getLatitude()
        alarm.longitude=currentSensorLocally.getLongtitude()

        alarm.isLocallyDefined=true

        val alarms=populateAlarmsFromLocally()
        alarms?.add(alarm)
        alarms?.let { storeAlarmsToLocally(it) }
    }


    //add not active alarm to history
    private fun addAlarmToHistory(
        isLocallyDefined: Boolean,
        alarmSensorName: String,
        isArmed: Boolean,
        alarmSensorId: String,
        type: String?
    ) {
        val tmp = Calendar.getInstance()
        val resources=this.resources
        val locale=if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) resources.configuration.locales.getFirstMatch(resources.assets.locales)
        else resources.configuration.locale
        val dateFormat= SimpleDateFormat("kk:mm dd/MM/yy", locale)
        val dateString=dateFormat.format(tmp.time)



        val alarm= Alarm(alarmSensorId,alarmSensorName,type, dateString,isArmed,tmp.timeInMillis)
        alarm.isLocallyDefined=isLocallyDefined

        val alarms=populateAlarmsFromLocally()
        alarms?.add(alarm)
        alarms?.let { storeAlarmsToLocally(it) }
    }

    //get locally sensor that match to sensor of alarm
    fun getLocallySensorAlarm(alarmSensorId: String): Camera? {
        val sensors: ArrayList<Camera>?
        val sensorsListStr= getStringInPreference(this, DETECTORS_LIST_KEY_PREF, ERROR_RESP)

        sensors = if(sensorsListStr.equals(ERROR_RESP)){
            ArrayList()
        }else {
            sensorsListStr?.let { convertJsonToSensorList(it) }
        }

        val iteratorList=sensors?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val detectorItem = iteratorList.next()
            if(alarmSensorId == detectorItem.getId()){
                return detectorItem
            }
        }
        return null
    }

    //get the detectors from locally
    private fun populateDetectorsFromLocally(): ArrayList<Camera>? {
        val detectors: ArrayList<Camera>?
        val detectorListStr= getStringInPreference(this, DETECTORS_LIST_KEY_PREF, ERROR_RESP)

        detectors = if(detectorListStr.equals(ERROR_RESP)){
            ArrayList()
        }else {
            detectorListStr?.let { convertJsonToSensorList(it) }
        }
        return detectors
    }

    //get the alarms from locally
    private fun populateAlarmsFromLocally(): ArrayList<Alarm>?  {
        val alarms: ArrayList<Alarm>?
        val alarmListStr= getStringInPreference(this, ALARM_LIST_KEY_PREF, ERROR_RESP)

        alarms = if(alarmListStr.equals(ERROR_RESP)){
            ArrayList()
        }else {
            alarmListStr?.let { convertJsonToAlarmList(it) }
        }
        return alarms
    }

    //store the detectors to locally
    private fun storeAlarmsToLocally(alarms: ArrayList<Alarm>){
        // sort the list of events by date in descending
        val alarms=ArrayList(alarms.sortedWith(compareByDescending { it.timeInMillis }))
        if(alarms!=null && alarms.size>0){
            val alarmsJsonStr= convertToAlarmsGson(alarms)
            setStringInPreference(this, ALARM_LIST_KEY_PREF,alarmsJsonStr)
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
                    .setContentText("SG-Hunter is running")
                    .setSmallIcon(getNotificationIcon())
                    .build()

                startForeground(1, notification)
            }
        }


}