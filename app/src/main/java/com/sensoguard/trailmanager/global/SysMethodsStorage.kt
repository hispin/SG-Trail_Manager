package com.sensoguard.trailmanager.global

import android.content.Context
import android.util.Log
import com.sensoguard.trailmanager.classes.Alarm
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.classes.MyEmailAccount

private const val APP_LOG_TAG = "TrailManager"

//get myEmail from locally
fun getMyEmailAccountFromLocally(context: Context): MyEmailAccount? {

    val myEmailAccountStr = getStringInPreference(context, EMAIL_ACCOUNT_KEY, null) ?: return null
    val myEmailAccount = convertJsonToMyEmailAccount(myEmailAccountStr) ?: return null
    //the password is stored encrypted
    val storedPassword = myEmailAccount.password
    myEmailAccount.password = decryptSecret(storedPassword)
    //migrate an old plain text password to encrypted
    if (!storedPassword.isNullOrEmpty() && !isEncryptedSecret(storedPassword)) {
        storeMyEmailAccountLocally(myEmailAccount, context)
    }
    return myEmailAccount
}

//store the sensors to locally
fun storeSensorsToLocally(sensors: ArrayList<Camera>, context: Context) {
    val detectorsJsonStr: String? = if (sensors.isNotEmpty()) convertToGson(sensors) else ""
    setStringInPreference(context, DETECTORS_LIST_KEY_PREF, detectorsJsonStr)
}

//get the sensors from locally
fun getCamerasFromLocally(activity: Context): ArrayList<Camera>? {
    val detectorListStr = getStringInPreference(activity, DETECTORS_LIST_KEY_PREF, ERROR_RESP)

    return if (detectorListStr == ERROR_RESP) {
        ArrayList()
    } else {
        detectorListStr?.let { convertJsonToSensorList(it) }
    }
}

//get the alarms from locally
fun getAlarmsFromLocally(context: Context): ArrayList<Alarm>? {
    val alarmListStr = getStringInPreference(context, ALARM_LIST_KEY_PREF, ERROR_RESP)

    return if (alarmListStr == ERROR_RESP) {
        ArrayList()
    } else {
        alarmListStr?.let { convertJsonToAlarmList(it) }
    }
}

//store the alarms to locally
fun storeAlarmsToLocally(alarms: ArrayList<Alarm>, context: Context) {
    // sort the list of events by date in descending
    val sortedAlarms = ArrayList(alarms.sortedWith(compareByDescending { it.timeInMillis }))
    setStringInPreference(context, ALARM_LIST_KEY_PREF, convertToAlarmsGson(sortedAlarms))
}

//store my email account locally
fun storeMyEmailAccountLocally(myEmailAccount: MyEmailAccount, context: Context) {
    //store a copy with an encrypted password ,the caller keeps the plain password
    val encryptedAccount = MyEmailAccount(
        myEmailAccount.emailAddress,
        encryptSecret(myEmailAccount.password),
        myEmailAccount.emailServer,
        myEmailAccount.emailPort,
        myEmailAccount.isUseSSL
    )
    setStringInPreference(context, EMAIL_ACCOUNT_KEY, convertToGson(encryptedAccount))
}

//write to the log (logcat)
fun writeLog(logMsg: String) {
    Log.d(APP_LOG_TAG, logMsg)
}
