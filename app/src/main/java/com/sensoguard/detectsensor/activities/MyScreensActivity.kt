package com.sensoguard.detectsensor.activities

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.ToggleButton
import com.sensoguard.detectsensor.fragments.HistoryWarningFragment
import com.sensoguard.detectsensor.fragments.MainUartFragment
import com.sensoguard.detectsensor.fragments.MapSensorsFragment
import com.sensoguard.detectsensor.interfaces.OnFragmentListener
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.sensoguard.detectsensor.global.*
import com.sensoguard.detectsensor.services.ServiceHandleAlarms
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.View
import com.google.android.material.tabs.TabLayout
import com.sensoguard.detectsensor.R
import com.sensoguard.detectsensor.classes.GeneralItemMenu
import com.sensoguard.detectsensor.classes.LanguageManager
import com.sensoguard.detectsensor.fragments.ConfigurationFragment
import com.sensoguard.detectsensor.services.ServiceConnectSensor
import kotlinx.android.synthetic.main.my_screens_activity.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference


class MyScreensActivity : AppCompatActivity(), OnFragmentListener {


    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var collectionPagerAdapter: CollectionPagerAdapter
    private lateinit var viewPager: ViewPager
    private var currentItemTopMenu = 0
    private var togChangeStatus: ToggleButton? = null


    val TAG = "MyScreensActivity"


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        //configurationLanguage()

        setContentView(R.layout.my_screens_activity)

        //store locally default values of configuration
        setConfigurationDefault()

        currentItemTopMenu = intent.getIntExtra(CURRENT_ITEM_TOP_MENU_KEY, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setLocationPermission()
        } else {
            init()
        }


    }


    //store locally default values of configuration
    private fun setConfigurationDefault() {

        if (getLongInPreference(this, ALARM_FLICKERING_DURATION_KEY, -1L) == -1L) {
            //set the duration of flickering icon when accepted alarm
            setLongInPreference(this, ALARM_FLICKERING_DURATION_KEY, ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS)
        }

        if (getIntInPreference(this, MAP_SHOW_VIEW_TYPE_KEY, -1) == -1) {
            //set the type of map
            setIntInPreference(this, MAP_SHOW_VIEW_TYPE_KEY, MAP_SHOW_SATELLITE_VALUE)
        }

        if (getStringInPreference(this, SELECTED_NOTIFICATION_SOUND_KEY, "-1").equals("-1")) {

            val uri=Uri.parse("android.resource://$packageName/raw/alarm_sound")

            setStringInPreference(this, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, arg1: Intent) {
            when {
                arg1.action == USB_CONNECTION_FAILED -> {
                    editActionBar(false)
                }

            }
        }
    }

    private fun setFilter() {
        val filter = IntentFilter(USB_CONNECTION_FAILED)

        registerReceiver(usbReceiver, filter)
    }


    private fun init() {

        configureActionBar()

        //start listener to alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ServiceHandleAlarms::class.java))
        } else {
            startService(Intent(this, ServiceHandleAlarms::class.java))
        }
        configTabs()

    }

//    @Override
//    protected override fun attachBaseContext(newBase:Context) {
//        configurationLanguage()
//    }

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

    private fun editActionBar(state: Boolean) {
        togChangeStatus?.isChecked = state
    }

    //TODO : the toggle will updated by the status changing
    private fun configureActionBar() {

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)//supportActionBar
        setSupportActionBar(toolbar)

        togChangeStatus = findViewById<ToggleButton>(
            R.id.togChangeStatus
        )
        togChangeStatus?.isChecked = false

        togChangeStatus?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(Intent(this, ServiceConnectSensor::class.java))
                } else {
                    startService(Intent(this, ServiceConnectSensor::class.java))
                }
            } else {
                sendBroadcast(Intent(STOP_READ_DATA_KEY))
            }
        }
    }


    private fun configTabs() {

        val tabs = findViewById<TabLayout>(R.id.tab_layout)

        viewPager = findViewById(R.id.vPager)

        collectionPagerAdapter = CollectionPagerAdapter(supportFragmentManager)
        viewPager.adapter = collectionPagerAdapter
        viewPager.setOnTouchListener(object : OnTouchListener {

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return true
            }
        })

        //relate the tab layout to viewpager because we need to add the icons
        tabs.setupWithViewPager(vPager)
        tabs.getTabAt(0)?.icon = ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_sensor_tab)
        tabs.getTabAt(1)?.icon = ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_map_tab)
        tabs.getTabAt(2)?.icon = ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_config_tab)
        tabs.getTabAt(3)?.icon = ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_alarm_log_tab)

        viewPager.currentItem = currentItemTopMenu


    }


    override fun onStart() {
        super.onStart()
        setFilter()
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(usbReceiver)
        } catch (ex: Exception) {

        }
    }


    private fun setLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setExternalPermission()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                setExternalPermission()
            }
            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init()
                }
            }
        }

    }


    private fun setExternalPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            init()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
        }
    }


    // Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
    inner class CollectionPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(
        fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

        override fun getCount(): Int = MAIN_MENU_NUM_ITEM

        override fun getItem(position: Int): Fragment {

            var fragment: Fragment? = null
            //set event of click ic_on top menu
            when (position) {
                0 -> {
                    fragment = MainUartFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", position + 1)
                    }
                }
                1 -> {
                    fragment = MapSensorsFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", position + 1)
                    }
                }
                2 -> {
                    fragment = ConfigurationFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", position + 1)
                    }
                }
                3 -> {
                    fragment = HistoryWarningFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", position + 1)
                    }
                }
            }
            return fragment!!

        }

        override fun getPageTitle(position: Int): CharSequence {

            //set the title text of top menu
            return when (position) {
                0 -> resources.getString(R.string.sensor_table_title)
                1 -> resources.getString(R.string.map_title)
                2 -> resources.getString(R.string.config_title)
                3 -> resources.getString(R.string.alarm_log_title)
                else -> "nothing"
            }

        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        //start activity for loading new language if it has been changed
        startActivity(Intent(this,MainActivity::class.java))
    }

    //set the language of the app (calling  from activity)
    override fun updateLanguage() {
        setAppLanguage(this, GeneralItemMenu.selectedItem)
        this.finish()
        this.startActivity(intent)
    }
}

//save the custom alarm file in alarms system
    class SaveCustomAlarmSoundAsync() : AsyncTask<Void, Void, Void>() {

        val LOG_TAG: String= "saveCustomAlarm"

        // Weak references will still allow the Activity to be garbage-collected
        private var weakActivity: WeakReference<Activity>?=null

        constructor(myActivity: Activity) : this() {
            weakActivity = WeakReference(myActivity)
        }


        override fun doInBackground(vararg params: Void?): Void? {
            saveCustomAlarmSound()
            return null
        }

        private fun saveCustomAlarmSound() {
            Environment.DIRECTORY_ALARMS

            // Get the directory for the app's private files directory.
            val dirTarget = File(
                weakActivity?.get()?.getExternalFilesDir(
                    Environment.DIRECTORY_ALARMS
                ), "alarm_sound.mp3"
            )

            if (!dirTarget.mkdirs()) {
                Log.e(LOG_TAG, "Directory not created")
            } else {

                val fileTarget = File(dirTarget.path + File.separator + "alarm_sound.mp3")
                val inputFileAlarm = weakActivity?.get()?.resources?.openRawResource(R.raw.alarm_sound)

                val byteArray = inputFileAlarm?.readBytes()


                try {
                    if(byteArray!=null) {
                        val fileOutput = FileOutputStream(fileTarget.absolutePath)
                        fileOutput.write(byteArray)
                        fileOutput.close()
                    }
                    //display file saved message
                    Log.e("Exception", "File saved successfully")

                } catch (e: IOException) {
                    Log.e("Exception", "File write failed: $e")
                }

            }

        }

    }



