package com.sensoguard.hunter.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.core.content.ContextCompat
import com.sensoguard.hunter.global.writeFile

class JobServiceRepeat : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        writeFile("start job service", this)
        //Log.d("checkJob","start job service")
        val serviceIntent = Intent(this, ServiceRepeat::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        return true
    }
}