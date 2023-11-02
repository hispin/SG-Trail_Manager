package com.sensoguard.trailmanager.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.sensoguard.trailmanager.classes.GeneralItemMenu
import com.sensoguard.trailmanager.classes.LanguageManager
import com.sensoguard.trailmanager.classes.MyExceptionHandler
import com.sensoguard.trailmanager.global.CURRENT_ITEM_TOP_MENU_KEY
import com.sensoguard.trailmanager.global.CURRENT_LANG_KEY_PREF
import com.sensoguard.trailmanager.global.IS_LOAD_APP
import com.sensoguard.trailmanager.global.getAppLanguage
import com.sensoguard.trailmanager.global.getStringInPreference
import com.sensoguard.trailmanager.global.setAppLanguage

//import io.fabric.sdk.android.Fabric

//import net.danlew.android.joda.JodaTimeAndroid


class MainActivity : AppCompatActivity() {

    private var clickConsCamerasTable: ConstraintLayout? = null
    private var clickConsMap: ConstraintLayout? = null
    private var clickConsConfiguration: ConstraintLayout? = null
    private var clickAlarmLog: ConstraintLayout? = null
    private var tvShowVer: AppCompatTextView? = null

//    @Override
//    protected override fun attachBaseContext(newBase:Context) {
//        configurationLanguage()
//    }


    override fun onStart() {
        super.onStart()
        //show version name
        val verName = packageManager.getPackageInfo(packageName, 0).versionName
        val verTitle = "version:$verName"
        tvShowVer?.text = verTitle
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        //var year=Calendar.getInstance().get(Calendar.YEAR)
        //JodaTimeAndroid.init(this)

        configureGeneralCatch()
        //Fabric.with(this, Crashlytics())

        super.onCreate(savedInstanceState)



        configurationLanguage()

        setContentView(com.sensoguard.trailmanager.R.layout.activity_main)

        //clearAllNotifications()


        //hide unwanted badge of app icon
        hideBudgetNotification()

        initViews()
        setOnClickSensorTable()
        setOnClickMapTable()
        setOnClickConfigTable()
        setOnClickAlarmLogTable()

        //hide status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        //start repeated timeout to scan alarms from incoming emails
        val isLoadApp = intent.getBooleanExtra(IS_LOAD_APP, false)
        if (isLoadApp) {
            //startServiceRepeat()
            //startJobServiceRepeat()
        }
    }

    override fun onResume() {
        super.onResume()
        clearAllNotifications()
    }

    //remove all notification of the app
    private fun clearAllNotifications() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }


    //hide unwanted badge of app icon
    private fun hideBudgetNotification() {
        val id = "my_channel_01"
        val name = getString(com.sensoguard.trailmanager.R.string.channel_name)
        val descriptionText = getString(com.sensoguard.trailmanager.R.string.channel_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel =
            NotificationChannel(id, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

    }

    private fun configureGeneralCatch() {
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(MyScreensActivity@ this))
    }

    private fun setOnClickSensorTable() {
        clickConsCamerasTable?.setOnClickListener {
            val inn=Intent(this,MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY,0)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }
    private fun setOnClickMapTable() {
        clickConsMap?.setOnClickListener{
            val inn=Intent(this,MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY,1)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }
    private fun setOnClickConfigTable() {
        clickConsConfiguration?.setOnClickListener{
            val inn=Intent(this,MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY,2)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }
    private fun setOnClickAlarmLogTable() {
        clickAlarmLog?.setOnClickListener{
            val inn=Intent(this,MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY,3)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }

    private fun initViews() {
        clickConsCamerasTable = findViewById(com.sensoguard.trailmanager.R.id.clickConsCamerasTable)
        clickConsMap = findViewById(com.sensoguard.trailmanager.R.id.clickConsMap)
        clickConsConfiguration = findViewById(com.sensoguard.trailmanager.R.id.clickConsConfiguration)
        clickAlarmLog = findViewById(com.sensoguard.trailmanager.R.id.clickAlarmLog)
        tvShowVer = findViewById(com.sensoguard.trailmanager.R.id.tvShowVer)
    }

    private fun configurationLanguage() {
        LanguageManager.setLanguageList()
        val currentLanguage = getStringInPreference(this, CURRENT_LANG_KEY_PREF, "-1")
        if (currentLanguage != "-1") {
            GeneralItemMenu.selectedItem = currentLanguage
            setAppLanguage(this, GeneralItemMenu.selectedItem)
        } else {
            val deviceLang = getAppLanguage()
            if (LanguageManager.isExistLang(deviceLang)) {
                GeneralItemMenu.selectedItem = deviceLang
                setAppLanguage(this, GeneralItemMenu.selectedItem)
            }
        }
    }

    //start repeated timeout to scan alarms from incoming emails
//    private fun startServiceRepeat() {
//        val serviceIntent = Intent(this, ServiceRepeat::class.java)
//        ContextCompat.startForegroundService(this, serviceIntent)
//
//
//    }

    //start job scheduler that supervision on serviceRepeat
//    private fun startJobServiceRepeat() {
//        Log.d("checkJob", "start from main")
//        val scheduler = this.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
//        val JOB_ID = 1
//
//        if (isBeenScheduled(JOB_ID)) {
//            Log.i("mainActivity", "scheduler.cancel(JOB_ID)")
//            scheduler.cancel(JOB_ID)
//        } else {
//
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                val jobInfo =
//                    JobInfo.Builder(0, ComponentName(this, JobServiceRepeat::class.java))
//                        .setPersisted(true)
//                        .setPeriodic(900000)
//                        .build()
//                scheduler.schedule(jobInfo)
//            } else {
//                val jobInfo =
//                    JobInfo.Builder(0, ComponentName(this, JobServiceRepeat::class.java))
//                        .setPersisted(true)
//                        .setPeriodic(900000)
//                        .build()
//
//                scheduler.schedule(jobInfo)
//            }
//        }
//
//    }

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
}
