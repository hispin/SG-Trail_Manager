package com.sensoguard.trailmanager.global

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private fun Context.appPreferences(): SharedPreferences =
    getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)

//read Long from shared preference
fun getLongInPreference(context: Context?, key: String, default: Long): Long {
    if (context == null) {
        return -1
    }
    return context.appPreferences().getLong(key, default)
}


//write Long to shared preference
fun setLongInPreference(context: Context?, key: String, value: Long) {
    context?.appPreferences()?.edit { putLong(key, value) }
}


//write String to shared preference
fun setStringInPreference(context: Context?, key: String, value: String?) {
    context?.appPreferences()?.edit { putString(key, value) }
}

//remove from shared preference
fun removePreference(context: Context?, key: String) {
    context?.appPreferences()?.edit { remove(key) }
}


//read String from shared preference
fun getStringInPreference(context: Context?, key: String, default: String?): String? {
    if (context == null) {
        return null
    }
    return context.appPreferences().getString(key, default)
}

//read Boolean from shared preference
fun getBooleanInPreference(context: Context?, key: String, default: Boolean): Boolean {
    if (context == null) {
        return false
    }
    return context.appPreferences().getBoolean(key, default)
}


//write Boolean to shared preference
fun setBooleanInPreference(context: Context?, key: String, value: Boolean) {
    context?.appPreferences()?.edit { putBoolean(key, value) }
}


//read Int from shared preference
fun getIntInPreference(context: Context?, key: String, default: Int): Int? {
    if (context == null) {
        return null
    }
    return context.appPreferences().getInt(key, default)
}


//write Int to shared preference
fun setIntInPreference(context: Context?, key: String, value: Int) {
    context?.appPreferences()?.edit { putInt(key, value) }
}
