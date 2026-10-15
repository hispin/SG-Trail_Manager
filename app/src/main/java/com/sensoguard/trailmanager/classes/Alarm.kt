package com.sensoguard.trailmanager.classes

import android.content.Context
import com.sensoguard.trailmanager.global.getAlarmsFromLocally
import com.sensoguard.trailmanager.global.storeAlarmsToLocally
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//an alarm (event) of a camera, stored locally as JSON (the field names are part of the stored data)
class Alarm {
    var isReadyToDelete: Boolean = false
    var id: String? = null
    var name: String? = null
    var type: String? = null
    var currentDate: String? = null
    var isArmed: Boolean? = null
    var timeInMillis: Long? = null

    var longitude: Double? = null
    var latitude: Double? = null
    var isLocallyDefined: Boolean = false
    var imgsPath: String? = null
    var isCameFromEmail = false
    var isLoadPhoto = false
    var msgNumber: Int? = null
    var batteryVal: Float? = null
    var wifiVal: Float? = null
    var fromEmail: String? = null

    constructor()


    constructor(
        id: String?,
        name: String?,
        type: String?,
        currentDate: String,
        isArmed: Boolean?,
        timeInMillis: Long?
    ) {

        this.id = id
        this.name = name
        this.type = type
        this.currentDate = currentDate
        this.isArmed = isArmed
        this.timeInMillis = timeInMillis
    }

    //add active alarm to history
    fun addAlarmToHistory(
        currentSensorLocally: Camera,
        type: String,
        context: Context,
        tmp: Calendar,
        batteryVal: Float,
        wifiVal: Float
    ) {
        val resources = context.resources
        val locale = resources.configuration.locales.getFirstMatch(resources.assets.locales)
        val dateFormat = SimpleDateFormat("kk:mm dd/MM/yyyy", locale ?: Locale.getDefault())

        this.id = currentSensorLocally.getId()
        this.name = currentSensorLocally.getName()
        this.type = type
        this.currentDate = dateFormat.format(tmp.time)
        this.isArmed = currentSensorLocally.isArmed()
        this.timeInMillis = tmp.timeInMillis
        this.latitude = currentSensorLocally.getLatitude()
        this.longitude = currentSensorLocally.getLongitude()
        this.batteryVal = batteryVal
        this.wifiVal = wifiVal
        this.isLocallyDefined = true

        val alarms = getAlarmsFromLocally(context)
        alarms?.add(this)
        alarms?.let { storeAlarmsToLocally(it, context) }
    }

    //update the image
    fun updateAlarm(imgPath: String, context: Context) {
        val alarms = getAlarmsFromLocally(context) ?: return

        for (item in alarms) {
            if (item.id == this.id && this.msgNumber == item.msgNumber) {
                item.isLoadPhoto = false
                item.imgsPath = imgPath
            }
        }

        storeAlarmsToLocally(alarms, context)
    }
}
