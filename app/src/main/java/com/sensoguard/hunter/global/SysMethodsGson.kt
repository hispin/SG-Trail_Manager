package com.sensoguard.hunter.global

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.sensoguard.hunter.classes.Alarm
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.classes.MyEmailAccount
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


//convert camera to json
fun convertToGson(myEmailAccount: MyEmailAccount): String? {
    try {
        val gSon = Gson()
        val data = gSon.toJson(myEmailAccount)
        val jsonElement = JsonParser().parse(data)
        return gSon.toJson(jsonElement)
        //TODO : how to get response about set shared preference

    } catch (ex: JSONException) {
        Log.i("exception", ex.message)
    } catch (ex: java.lang.Exception) {
        Log.i("exception", ex.message)
    }
    return ERROR_RESP
}


//convert camera to json
fun convertToGson(camera: Camera): String? {
    try {
        val gSon = Gson()
        val data = gSon.toJson(camera)
        val jsonElement = JsonParser().parse(data)
        return gSon.toJson(jsonElement)
        //TODO : how to get response about set shared preference

    } catch (ex: JSONException) {
        Log.i("exception", ex.message)
    } catch (ex: java.lang.Exception) {
        Log.i("exception", ex.message)
    }
    return ERROR_RESP
}

//convert json to Camera
fun convertJsonToSensor(inputJsonString: String): Camera? {

    //if the json string is empty, then return empty array list
    if (inputJsonString.isNullOrEmpty()) {
        return null
    }

    var mySensor: Camera? = null
    //mySensors?.add(Camera("ID","NAME"))

    var json: JSONObject? = null
    try {
        json = JSONObject(inputJsonString)
    } catch (e: JSONException) {
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    }

    try {
        val listType = object : TypeToken<Camera>() {

        }.type
        mySensor = Gson().fromJson(json.toString(), listType) as Camera
    } catch (e: JsonIOException) {
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    } catch (e: JSONException) {
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    }

    return mySensor
    //when jsonArr is null will return value of new ArrayList<>()
}//convertJsonToUriList

//convert json to Camera
fun convertJsonToMyEmailAccount(inputJsonString: String): MyEmailAccount? {

    //if the json string is empty, then return empty array list
    if (inputJsonString.isNullOrEmpty()) {
        return null
    }

    var myEmailAccount: MyEmailAccount? = null
    //mySensors?.add(Camera("ID","NAME"))

    var json: JSONObject? = null
    try {
        json = JSONObject(inputJsonString)
    } catch (e: JSONException) {
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    }

    try {
        val listType = object : TypeToken<MyEmailAccount>() {

        }.type
        myEmailAccount = Gson().fromJson(json.toString(), listType) as MyEmailAccount
    } catch (e: JsonIOException) {
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    } catch (e: JSONException) {
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    }

    return myEmailAccount
    //when jsonArr is null will return value of new ArrayList<>()
}//convertJsonToUriList


fun convertToGson(detectorsArr: ArrayList<Camera>): String? {
    try {
        val gSon= Gson()
        val data=gSon.toJson(detectorsArr)
        val jsonArray= JsonParser().parse(data).asJsonArray
        return gSon.toJson(jsonArray)
        //TODO : how to get response about set shared preference

    } catch (ex: JSONException) {
        Log.i("exception", ex.message)
    } catch (ex: java.lang.Exception) {
        Log.i("exception", ex.message)
    }
    return ERROR_RESP
}

fun convertToAlarmsGson(alarmsArr:ArrayList<Alarm>): String? {
    try {
        val gSon= Gson()
        val data=gSon.toJson(alarmsArr)
        val jsonArray= JsonParser().parse(data).asJsonArray
        return gSon.toJson(jsonArray)
        //TODO : how to get response about set shared preference

    } catch (ex: JSONException) {
        Log.i("exception", ex.message)
    } catch (ex: java.lang.Exception) {
        Log.i("exception", ex.message)
    }
    return ERROR_RESP
}

//convert json to list of uri and list of Sensors
fun convertJsonToSensorList(inputJsonArrayString: String): ArrayList<Camera>? {

    //if the json string is empty, then return empty array list
    if(inputJsonArrayString.isNullOrEmpty()){
        return ArrayList()
    }

    val mySensors: ArrayList<Camera>? = ArrayList()
    //mySensors?.add(Camera("ID","NAME"))

    var jsonArr: JSONArray?=null
    try {
        jsonArr= JSONArray(inputJsonArrayString)
    } catch (e: JSONException) {
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    }

    try {
        val listType = object : TypeToken<List<Camera>>() {

        }.type
        mySensors?.addAll(Gson().fromJson(jsonArr.toString(), listType) as ArrayList<Camera>)
    }catch(e:JsonIOException){
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    }catch(e:JsonSyntaxException){
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    }catch (e: JSONException) {
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    }

    return mySensors
    //when jsonArr is null will return value of new ArrayList<>()
}//convertJsonToUriList

//convert json to list of uri and list of Alarms
fun convertJsonToAlarmList(inputJsonArrayString: String): ArrayList<Alarm>? {

    var myAlarms:ArrayList<Alarm>?=null
    var jsonArr: JSONArray?=null
    try {
        jsonArr= JSONArray(inputJsonArrayString)
    } catch (e: JSONException) {
        e.printStackTrace()
        Log.e("convertJsonToUriList", e.message)
    }

    try {
        val listType = object : TypeToken<List<Alarm>>() {

        }.type
        myAlarms = Gson().fromJson(jsonArr.toString(), listType) as ArrayList<Alarm>
    }catch(e:JsonIOException){
        e.printStackTrace()
        Log.e("convertJsonToAlarmList", e.message)
    }catch(e:JsonSyntaxException){
        e.printStackTrace()
        Log.e("convertJsonToAlarmList", e.message)
    }

    return myAlarms
    //when jsonArr is null will return value of new ArrayList<>()
}