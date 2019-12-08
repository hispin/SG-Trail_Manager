package com.sensoguard.hunter.classes

import com.google.android.gms.maps.model.Marker
import java.util.*

class AlarmSensor (var alarmSensorId:String, var alarmTime: Calendar,var type:String){
    var marker: Marker?=null
}