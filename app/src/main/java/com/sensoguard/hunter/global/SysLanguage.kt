package com.sensoguard.hunter.global

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*


//Get the current language of the app
fun getAppLanguage(): String {
    return Locale.getDefault().language
}

//set language for the application
fun setAppLanguage(c: Context, lang: String) {
    val localeNew = Locale(lang)
    Locale.setDefault(localeNew)

    val res = c.resources
    val newConfig = Configuration(res.configuration)
    newConfig.locale = localeNew
    newConfig.setLayoutDirection(localeNew)
    res.updateConfiguration(newConfig, res.displayMetrics)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        newConfig.setLocale(localeNew)
        c.createConfigurationContext(newConfig)
    }
}




