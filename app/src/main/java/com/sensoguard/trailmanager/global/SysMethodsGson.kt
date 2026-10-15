package com.sensoguard.trailmanager.global

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.sensoguard.trailmanager.classes.Alarm
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.classes.MyEmailAccount

private const val GSON_LOG_TAG = "SysMethodsGson"

private val gson = Gson()

//convert an object to JSON, returns ERROR_RESP if failed
private fun toJsonOrError(value: Any): String? {
    return try {
        gson.toJson(value)
    } catch (ex: Exception) {
        Log.e(GSON_LOG_TAG, "toJson failed", ex)
        ERROR_RESP
    }
}

//convert JSON to an object, returns null if the JSON is empty or invalid
private inline fun <reified T> fromJsonOrNull(json: String?): T? {
    if (json.isNullOrEmpty()) {
        return null
    }
    return try {
        gson.fromJson<T>(json, object : TypeToken<T>() {}.type)
    } catch (ex: JsonParseException) {
        Log.e(GSON_LOG_TAG, "fromJson failed", ex)
        null
    }
}

//convert email account to JSON
fun convertToGson(myEmailAccount: MyEmailAccount): String? = toJsonOrError(myEmailAccount)

//convert camera to JSON
fun convertToGson(camera: Camera): String? = toJsonOrError(camera)

//convert list of cameras to JSON
fun convertToGson(detectorsArr: ArrayList<Camera>): String? = toJsonOrError(detectorsArr)

//convert list of alarms to JSON
fun convertToAlarmsGson(alarmsArr: ArrayList<Alarm>): String? = toJsonOrError(alarmsArr)

//convert JSON to Camera
fun convertJsonToSensor(inputJsonString: String): Camera? = fromJsonOrNull<Camera>(inputJsonString)

//convert JSON to email account
fun convertJsonToMyEmailAccount(inputJsonString: String): MyEmailAccount? =
    fromJsonOrNull<MyEmailAccount>(inputJsonString)

//convert JSON to list of cameras (an empty JSON is an empty list)
fun convertJsonToSensorList(inputJsonArrayString: String): ArrayList<Camera>? {
    if (inputJsonArrayString.isEmpty()) {
        return ArrayList()
    }
    return fromJsonOrNull<ArrayList<Camera>>(inputJsonArrayString)
}

//convert JSON to list of alarms
fun convertJsonToAlarmList(inputJsonArrayString: String): ArrayList<Alarm>? =
    fromJsonOrNull<ArrayList<Alarm>>(inputJsonArrayString)
