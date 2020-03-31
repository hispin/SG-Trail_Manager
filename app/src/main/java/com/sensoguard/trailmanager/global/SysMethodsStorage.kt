package com.sensoguard.trailmanager.global

import android.content.Context
import com.sensoguard.trailmanager.classes.Alarm
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.classes.MyEmailAccount
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
fun getCamerasFromLocally(activity: Context): ArrayList<Camera>? {
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
    if (alarms != null) {
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

//write to log
fun writeFile(logMsg: String, context: Context) {
//    val thread = object : Thread() {
//        override fun run() {
//            val externalStorageDir = Environment.getExternalStorageDirectory()
//            val myFile = File(externalStorageDir, "hunter_logs.txt")
//
//            if (myFile.exists()) {
//                try {
//                    val currDateStr =
//                        getStringFromCalendar(Calendar.getInstance(), "dd/MM/yy kk:mm:ss", context)
//                    val fostream = FileOutputStream(myFile, true)
//                    val oswriter = OutputStreamWriter(fostream)
//                    val bwriter = BufferedWriter(oswriter)
//                    bwriter.write("$currDateStr:log:$logMsg")
//                    bwriter.newLine()
//                    bwriter.close()
//                    oswriter.close()
//                    fostream.close()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            } else {
//                try {
//                    myFile.createNewFile()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//
//        }
//    }
//
//    thread.start()

}