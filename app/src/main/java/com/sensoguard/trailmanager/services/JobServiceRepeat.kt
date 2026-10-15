package com.sensoguard.trailmanager.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.core.content.ContextCompat
import com.sensoguard.trailmanager.global.writeLog

class JobServiceRepeat : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        writeLog("start job service")
        //Log.d("checkJob","start job service")
        val serviceIntent = Intent(this, ServiceRepeat::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        return true
    }
}