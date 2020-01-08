package com.sensoguard.hunter.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sensoguard.hunter.classes.EmailsManage
import com.sensoguard.hunter.global.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

open class ServiceRepeat : Service() {

    private var scheduleTaskExecutor: ScheduledExecutorService? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun setFilter() {
        val filter = IntentFilter(CREATE_ALARM_KEY)
        registerReceiver(emailReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(emailReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
            try {
                val thread = object : Thread() {
                    override fun run() {


                        val cameras = getSensorsFromLocally(this@ServiceRepeat)

                        val iteratorList = cameras?.listIterator()
                        while (iteratorList != null && iteratorList.hasNext()) {
                            val cameraItem = iteratorList.next()
//                            if(!cameraItem.emailServer.isNullOrEmpty()) {
//                                Log.d("testCamera", "id=" + cameraItem.emailServer)
//                            }
                            if (!cameraItem.emailServer.isNullOrEmpty()) {
                                EmailsManage.getInstance()
                                    .readeLastDayUnreadEmails(cameraItem, this@ServiceRepeat)
                            }
                        }


                    }
                }

                thread.start()
            } catch (ex: Exception) {
                Log.d("testTimer", "exception:" + ex.message)
            }

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

        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val `object` = getSystemService(Context.NOTIFICATION_SERVICE)
            if (`object` != null && `object` is NotificationManager) {
                `object`.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()

            startForeground(1, notification)
        }
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


}