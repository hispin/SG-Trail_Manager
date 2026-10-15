package com.sensoguard.trailmanager.global

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

//the locale of the app resources (used to format dates)
private fun getResourcesLocale(context: Context): Locale? =
    context.resources.configuration.locales.getFirstMatch(context.resources.assets.locales)

fun getStringFromCalendar(calendar: Calendar, format: String, context: Context): String {
    val dateFormat = SimpleDateFormat(
        format,
        getResourcesLocale(context) ?: Locale.getDefault()
    )//"kk:mm dd/MM/yy"
    return dateFormat.format(calendar.time)
}

//get string format by current date time in milliseconds format
fun getStrDateTimeByMilliSeconds(milliSeconds: Long, format: String, context: Context): String {
    val dateFormat = SimpleDateFormat(format, getResourcesLocale(context) ?: Locale.getDefault())
    return dateFormat.format(Date(milliSeconds))
}
