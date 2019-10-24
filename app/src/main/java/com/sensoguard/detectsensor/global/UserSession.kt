package com.sensoguard.detectsensor.global

import com.sensoguard.detectsensor.classes.AlarmSensor

class UserSession private constructor() {

    //list of sensors alarm
    var alarmSensors: ArrayList<AlarmSensor>?=ArrayList()

    private object Holder {
        val INSTANCE=UserSession()
    }

    companion object {
        val instance: UserSession by lazy { Holder.INSTANCE }
    }
}