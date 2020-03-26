package com.sensoguard.hunter.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.classes.EmailsManage
import com.sensoguard.hunter.global.CAMERA_KEY
import com.sensoguard.hunter.global.RESULT_VALIDATION_EMAIL_ACTION
import com.sensoguard.hunter.global.VALIDATION_EMAIL_RESULT
import com.sensoguard.hunter.global.convertJsonToSensor

class ServiceEmailValidation1 : Service() {

    private var myCamera: Camera? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(myIntent: Intent?, flags: Int, startId: Int): Int {
        startSysForeGround()

        val cameraStr = myIntent?.extras?.getString(CAMERA_KEY, null)
        cameraStr?.let { myCamera = convertJsonToSensor(cameraStr) }

        object : Thread() {
            override fun run() {
                super.run()
                val resultConnection = EmailsManage.getInstance()
                    .emailValidation(myCamera, this@ServiceEmailValidation1)
                val inn = Intent(RESULT_VALIDATION_EMAIL_ACTION)
                inn.putExtra(VALIDATION_EMAIL_RESULT, resultConnection)
                sendBroadcast(inn)
                stopSelf()
            }
        }.start()

        return START_STICKY
    }


    //The system allows apps to call Context.startForegroundService() even while the app is in the background. However, the app must call that service's startForeground() method within five seconds after the service is created
    private fun startSysForeGround() {
        fun getNotificationIcon(): Int {
            val useWhiteIcon =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            return if (useWhiteIcon) R.drawable.ic_app_notification else R.mipmap.ic_launcher
        }
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
                .setContentText("SG-Hunter is running")
                .setSmallIcon(getNotificationIcon())
                .build()

            startForeground(1, notification)
        }
    }

}