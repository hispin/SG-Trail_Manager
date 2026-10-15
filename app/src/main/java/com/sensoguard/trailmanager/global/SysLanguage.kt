package com.sensoguard.trailmanager.global

import android.content.Context
import android.content.res.Configuration
import java.util.Locale


//Get the current language of the app
fun getAppLanguage(): String {
    return Locale.getDefault().language
}

//set language for the application
//the app is always shown in hebrew (TODO: if selecting a language will be enabled then change here)
fun setAppLanguage(c: Context) {
    val localeNew = Locale.forLanguageTag("iw")
    Locale.setDefault(localeNew)

    val res = c.resources
    val newConfig = Configuration(res.configuration)
    newConfig.setLocale(localeNew)
    newConfig.setLayoutDirection(localeNew)
    //the app changes the locale of the existing resources (all activities use them),
    //createConfigurationContext would require overriding attachBaseContext in every activity
    @Suppress("DEPRECATION")
    res.updateConfiguration(newConfig, res.displayMetrics)
}
