package com.sensoguard.trailmanager.services

import android.content.Intent

import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.sensoguard.trailmanager.classes.EmailsManage
import com.sensoguard.trailmanager.classes.MyEmailAccount
import com.sensoguard.trailmanager.global.CAMERA_KEY
import com.sensoguard.trailmanager.global.RESULT_VALIDATION_EMAIL_ACTION
import com.sensoguard.trailmanager.global.VALIDATION_EMAIL_RESULT
import com.sensoguard.trailmanager.global.convertJsonToMyEmailAccount
import com.sensoguard.trailmanager.global.FOREGROUND_TYPE_DATA_SYNC
import com.sensoguard.trailmanager.global.startForegroundWithNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//check the connection to the email account (IMAP) in the background and broadcast the result
class ServiceEmailValidation : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startSysForeGround()

        val myEmailAccount: MyEmailAccount? =
            intent?.extras?.getString(CAMERA_KEY, null)?.let { convertJsonToMyEmailAccount(it) }

        lifecycleScope.launch {
            val resultConnection = withContext(Dispatchers.IO) {
                EmailsManage.getInstance()
                    .emailValidation(myEmailAccount, this@ServiceEmailValidation)
            }
            val inn = Intent(RESULT_VALIDATION_EMAIL_ACTION)
            inn.setPackage(packageName)
            inn.putExtra(VALIDATION_EMAIL_RESULT, resultConnection)
            sendBroadcast(inn)
            stopSelf(startId)
        }

        //a one time check, do not restart the service if it is killed
        return START_NOT_STICKY
    }

    private fun startSysForeGround() {
        startForegroundWithNotification(FOREGROUND_TYPE_DATA_SYNC)
    }

}
