package com.sensoguard.trailmanager.global

import com.sensoguard.trailmanager.classes.AlarmSensor

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