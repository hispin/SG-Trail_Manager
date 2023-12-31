package com.sensoguard.trailmanager.services

import android.app.NotificationChannel
import android.app.NotificationManager
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
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.EmailsManage
import com.sensoguard.trailmanager.global.CREATE_ALARM_KEY
import com.sensoguard.trailmanager.global.HUNTER_LOG
import com.sensoguard.trailmanager.global.IS_EMAIL_CONFIG_PREF_KEY
import com.sensoguard.trailmanager.global.IS_NOTIFICATION_SOUND_KEY
import com.sensoguard.trailmanager.global.IS_VIBRATE_WHEN_ALARM_KEY
import com.sensoguard.trailmanager.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.trailmanager.global.getBooleanInPreference
import com.sensoguard.trailmanager.global.getMyEmailAccountFromLocally
import com.sensoguard.trailmanager.global.getStringInPreference
import com.sensoguard.trailmanager.global.writeFile
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

open class ServiceRepeat : Service() {

    private var scheduleTaskExecutor: ScheduledExecutorService? = null
    //to prevent start scanning before the previous process is completed
    private var isScanning = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun setFilter() {
        val filter = IntentFilter(CREATE_ALARM_KEY)
        registerReceiver(emailReceiver, filter)
    }

    override fun onDestroy() {
        //Log.d("checkJob","service destroy")
        super.onDestroy()
        try {
            unregisterReceiver(emailReceiver)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        startJobServiceRepeat()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        //Log.d("checkJob","on low memory")
        startJobServiceRepeat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Log.d("checkJob","service start")
        writeFile("start timer service", this)
        setFilter()
        startSysForeGround()
        shutDownTimer()
        startTimer()
        return START_STICKY
    }

    private fun startTimer() {
        //if there is already at least one alarm ,it is not necessary to initial the timer
        if (scheduleTaskExecutor == null
            || (scheduleTaskExecutor?.isShutdown != null && scheduleTaskExecutor?.isShutdown!!)
        ) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1)
            executeTimer()
        }
    }

    // execute the time
    private fun executeTimer() {

        // This schedule a task to run every 1 second:
        scheduleTaskExecutor?.scheduleAtFixedRate({


            stopPlayingAlarm()

            Log.d("testEmails", "try to start scan")

            val isEmailConfig =
                getBooleanInPreference(this@ServiceRepeat, IS_EMAIL_CONFIG_PREF_KEY, false)
            if (!isEmailConfig) {
                return@scheduleAtFixedRate
            }

            //prevent scanning  if the network is not connected
            val isNetWorkConnected = checkNetworkState()
            if (isNetWorkConnected && !isScanning) {
                //Log.d("testEmails", "start scan")
                try {
                    isScanning = true
                    val thread = object : Thread() {
                        override fun run() {

                            val myEmailAccount = getMyEmailAccountFromLocally(this@ServiceRepeat)
                            EmailsManage.getInstance().readeLastDayUnreadEmails(
                                myEmailAccount,
                                this@ServiceRepeat
                            )
                            isScanning = false
                        }
                    }

                    if (Build.VERSION.SDK_INT <= 32) {
                        thread.start()
                    } else {

                    }
                } catch (ex: Exception) {
                    //write to log
                    writeFile("exception in executeTimer:" + ex.message, this)
                    Log.d(HUNTER_LOG, "exception in executeTimer:" + ex.message)
                    Log.d("testEmails", "exception in executeTimer:" + ex.message)
                }
            } else {
                if (!isNetWorkConnected) {
                    Log.d("testEmails", "network is not connected")
                    //write to log
                    writeFile("not connected", this)
                }
            }//end of checkNetworkState


        }, 0, 20, TimeUnit.SECONDS)

    }

    //shut down the timer
    private fun shutDownTimer() {
        try {
            scheduleTaskExecutor?.shutdownNow()
        } catch (ex: Exception) {
        }
    }


    //The system allows apps to call Context.startForegroundService() even while the app is in the background. However, the app must call that service's startForeground() method within five seconds after the service is created
    private fun startSysForeGround() {
        fun getNotificationIcon(): Int {
            val useWhiteIcon =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            return if (useWhiteIcon) R.drawable.ic_app_notification else R.mipmap.ic_launcher
        }

        val CHANNEL_ID = "my_channel_01"
        if (Build.VERSION.SDK_INT >= 26) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val `object` = getSystemService(Context.NOTIFICATION_SERVICE)
            if (`object` != null && `object` is NotificationManager) {
                `object`.createNotificationChannel(channel)
            }
        }
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("SG-Trail Manager is running")
                .setSmallIcon(getNotificationIcon())
                .build()

            startForeground(1, notification)

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

        val isVibrateWhenAlarm =
            getBooleanInPreference(this@ServiceRepeat, IS_VIBRATE_WHEN_ALARM_KEY, true)
        if (isVibrateWhenAlarm) {
            // Get instance of Vibrator from current Context
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            // Vibrate for 200 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        1000,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(1000)
            }

        }

    }


    private var rington: Ringtone? = null

    //stop play the sound of alarm
    private fun stopPlayingAlarm() {
        if (rington != null && rington?.isPlaying!!) {
            rington?.stop()
        }
    }

    //execute vibrate
    private fun playAlarmSound() {

        val isNotificationSound =
            getBooleanInPreference(this@ServiceRepeat, IS_NOTIFICATION_SOUND_KEY, true)
        if (!isNotificationSound) {
            return
        }

        val selectedSound =
            getStringInPreference(this@ServiceRepeat, SELECTED_NOTIFICATION_SOUND_KEY, "-1")

        if (!selectedSound.equals("-1")) {

            try {
                val uri = Uri.parse(selectedSound)
                if (rington != null && rington!!.isPlaying) {
                    //if the sound it is already played,
                    rington?.stop()
                    Handler().postDelayed({
                        rington = RingtoneManager.getRingtone(this@ServiceRepeat, uri)
                        rington?.play()
                    }, 1000)
                } else {
                    rington = RingtoneManager.getRingtone(this@ServiceRepeat, uri)
                    rington?.play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    //start job scheduler that supervision on serviceRepeat
    private fun startJobServiceRepeat() {
        Log.d("checkJob", "start from service")
        val scheduler = this.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val JOB_ID = 1

        if (isBeenScheduled(JOB_ID)) {
            Log.i("mainActivity", "scheduler.cancel(JOB_ID)")
            scheduler.cancel(JOB_ID)
        } else {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                val jobInfo =
                    JobInfo.Builder(0, ComponentName(this, JobServiceRepeat::class.java))
                        .setPersisted(true)
                        .setPeriodic(900000)
                        .build()
                scheduler.schedule(jobInfo)
            } else {
                val jobInfo =
                    JobInfo.Builder(0, ComponentName(this, JobServiceRepeat::class.java))
                        .setPersisted(true)
                        .setPeriodic(900000)
                        .build()

                scheduler.schedule(jobInfo)
            }
        }

    }

    // check if this schedule with JOB_ID is active
    private fun isBeenScheduled(JOB_ID: Int): Boolean {
        val scheduler = this.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        var hasBeenScheduled = false
        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == JOB_ID) {
                hasBeenScheduled = true
            }
        }
        return hasBeenScheduled
    }

    //check network connection
    private fun checkNetworkState(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }
    }
}