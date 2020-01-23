package com.sensoguard.hunter.global

import android.content.Context
import com.sensoguard.hunter.classes.Alarm
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.classes.MyEmailAccount
import java.lang.ref.WeakReference

//get myEmail from locally
fun getMyEmailAccountFromLocally(context: Context): MyEmailAccount? {

    val myEmailAccountStr = getStringInPreference(context, EMAIL_ACCOUNT_KEY, null)
    myEmailAccountStr?.let {
        var myEmailAccount = convertJsonToMyEmailAccount(myEmailAccountStr)
        return myEmailAccount
    }
    return null
}

//store the sensors to locally
fun storeSensorsToLocally(sensors: ArrayList<Camera>, context: Context) {

    var detectorsJsonStr:String?=""
    if(sensors!=null && sensors.size>0){
        detectorsJsonStr= convertToGson(sensors)
    }
    setStringInPreference(context,DETECTORS_LIST_KEY_PREF,detectorsJsonStr)
}

//get the sensors from locally
fun getSensorsFromLocally(activity: Context): ArrayList<Camera>? {
    val sensors: ArrayList<Camera>?
    val detectorListStr=getStringInPreference(activity,DETECTORS_LIST_KEY_PREF, ERROR_RESP)

    sensors = if(detectorListStr.equals(ERROR_RESP)){
        ArrayList()
    }else {
        detectorListStr?.let { convertJsonToSensorList(it) }
    }
    return sensors
}

//get the alarms from locally
fun getAlarmsFromLocally(context: Context): java.util.ArrayList<Alarm>? {
    //use WeakReference if the activity is no longer alive
    val wContext: WeakReference<Context> =
        WeakReference(context)
    val alarms: java.util.ArrayList<Alarm>?
    val alarmListStr = getStringInPreference(wContext.get(), ALARM_LIST_KEY_PREF, ERROR_RESP)

    alarms = if (alarmListStr.equals(ERROR_RESP)) {
        java.util.ArrayList()
    } else {
        alarmListStr?.let { convertJsonToAlarmList(it) }
    }
    return alarms
}

//store the detectors to locally
fun storeAlarmsToLocally(alarms: java.util.ArrayList<Alarm>, context: Context) {
    //use WeakReference if the activity is no longer alive
    val wContext: WeakReference<Context> =
        WeakReference(context)
    // sort the list of events by date in descending
    val alarms = java.util.ArrayList(alarms.sortedWith(compareByDescending { it.timeInMillis }))
    if (alarms != null && alarms.size > 0) {
        val alarmsJsonStr = convertToAlarmsGson(alarms)
        setStringInPreference(wContext.get(), ALARM_LIST_KEY_PREF, alarmsJsonStr)
    }
}

//store the my email account to locally
fun storeMyEmailAccountToLocaly(myEmailAccount: MyEmailAccount, context: Context) {
    //use WeakReference if the activity is no longer alive
    val wContext: WeakReference<Context> =
        WeakReference(context)

    if (myEmailAccount != null) {
        val myEmailAccountStr = convertToGson(myEmailAccount)
        setStringInPreference(wContext.get(), EMAIL_ACCOUNT_KEY, myEmailAccountStr)
    }
}