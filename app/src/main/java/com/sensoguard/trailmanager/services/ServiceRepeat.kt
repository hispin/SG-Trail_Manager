package com.sensoguard.trailmanager.services

import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.sensoguard.trailmanager.classes.EmailsManage
import com.sensoguard.trailmanager.global.CREATE_ALARM_KEY
import com.sensoguard.trailmanager.global.FOREGROUND_TYPE_DATA_SYNC
import com.sensoguard.trailmanager.global.HUNTER_LOG
import com.sensoguard.trailmanager.global.IS_EMAIL_CONFIG_PREF_KEY
import com.sensoguard.trailmanager.global.IS_NOTIFICATION_SOUND_KEY
import com.sensoguard.trailmanager.global.IS_VIBRATE_WHEN_ALARM_KEY
import com.sensoguard.trailmanager.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.trailmanager.global.getBooleanInPreference
import com.sensoguard.trailmanager.global.getMyEmailAccountFromLocally
import com.sensoguard.trailmanager.global.getStringInPreference
import com.sensoguard.trailmanager.global.startForegroundWithNotification
import com.sensoguard.trailmanager.global.writeLog
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

//the id of the job that restarts this service
private const val REPEAT_JOB_ID = 0

//the interval of the job that restarts this service (15 minutes)
private const val REPEAT_JOB_INTERVAL_MILLIS = 900000L

//scan the emails of the cameras every 20 seconds (background service)
open class ServiceRepeat : Service() {

    private var scheduleTaskExecutor: ScheduledExecutorService? = null

    //to prevent start scanning before the previous process is completed
    @Volatile
    private var isScanning = false
    private var ringtone: Ringtone? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(emailReceiver)
        } catch (ex: IllegalArgumentException) {
            Log.w(HUNTER_LOG, "the receiver is not registered", ex)
        }
        startJobServiceRepeat()
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        @Suppress("DEPRECATION")
        super.onLowMemory()
        startJobServiceRepeat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        writeLog("start timer service")
        ContextCompat.registerReceiver(
            this,
            emailReceiver,
            IntentFilter(CREATE_ALARM_KEY),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        startForegroundWithNotification(FOREGROUND_TYPE_DATA_SYNC)
        shutDownTimer()
        startTimer()
        return START_STICKY
    }

    private fun startTimer() {
        //if there is already a running timer ,it is not necessary to initial the timer
        if (scheduleTaskExecutor == null || scheduleTaskExecutor?.isShutdown == true) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1)
            executeTimer()
        }
    }

    //scan the emails (on the thread of the timer)
    private fun scanEmails() {
        stopPlayingAlarm()

        if (!getBooleanInPreference(this, IS_EMAIL_CONFIG_PREF_KEY, false)) {
            return
        }

        //prevent scanning if the network is not connected
        if (!checkNetworkState()) {
            writeLog("not connected")
            return
        }

        //the scan is enabled up to Android 12L (API 32)
        if (isScanning || Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            return
        }

        isScanning = true
        try {
            val myEmailAccount = getMyEmailAccountFromLocally(this)
            EmailsManage.getInstance().readeLastDayUnreadEmails(myEmailAccount, this)
        } catch (ex: Exception) {
            writeLog("exception in executeTimer:" + ex.message)
            Log.w(HUNTER_LOG, "exception in executeTimer", ex)
        } finally {
            isScanning = false
        }
    }

    // execute the timer every 20 seconds
    // (fixed delay: after the device sleeps the missed scans are not executed all at once)
    private fun executeTimer() {
        scheduleTaskExecutor?.scheduleWithFixedDelay({ scanEmails() }, 0, 20, TimeUnit.SECONDS)
    }

    //shut down the timer
    private fun shutDownTimer() {
        scheduleTaskExecutor?.shutdownNow()
    }

    private val emailReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, inn: Intent) {
            //accept currentAlarm
            if (inn.action == CREATE_ALARM_KEY) {
                playAlarmSound()
                playVibrate()
                startTimer()
            }
        }
    }

    //execute vibrate
    private fun playVibrate() {
        if (getBooleanInPreference(this, IS_VIBRATE_WHEN_ALARM_KEY, true)) {
            // Vibrate for 1 second
            getSystemService(Vibrator::class.java)
                ?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    //stop play the sound of alarm
    private fun stopPlayingAlarm() {
        if (ringtone?.isPlaying == true) {
            ringtone?.stop()
        }
    }

    //play the sound of alarm
    private fun playAlarmSound() {

        if (!getBooleanInPreference(this, IS_NOTIFICATION_SOUND_KEY, true)) {
            return
        }

        val selectedSound = getStringInPreference(this, SELECTED_NOTIFICATION_SOUND_KEY, "-1")
        if (selectedSound == null || selectedSound == "-1") {
            return
        }

        try {
            val uri = selectedSound.toUri()
            if (ringtone?.isPlaying == true) {
                //if the sound it is already played, play it again after 1 second
                ringtone?.stop()
                Handler(Looper.getMainLooper()).postDelayed({
                    ringtone = RingtoneManager.getRingtone(this, uri)
                    ringtone?.play()
                }, 1000)
            } else {
                ringtone = RingtoneManager.getRingtone(this, uri)
                ringtone?.play()
            }
        } catch (e: Exception) {
            Log.w(HUNTER_LOG, "play alarm sound failed", e)
        }

    }

    //start job scheduler that supervision on serviceRepeat
    private fun startJobServiceRepeat() {
        val scheduler = getSystemService(JobScheduler::class.java) ?: return
        val jobInfo =
            JobInfo.Builder(REPEAT_JOB_ID, ComponentName(this, JobServiceRepeat::class.java))
                .setPersisted(true)
                .setPeriodic(REPEAT_JOB_INTERVAL_MILLIS)
                .build()
        scheduler.schedule(jobInfo)
    }

    //check network connection
    private fun checkNetworkState(): Boolean {
        val connectivityManager = getSystemService(ConnectivityManager::class.java) ?: return false
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
