package com.sensoguard.trailmanager.activities

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.GeneralItemMenu
import com.sensoguard.trailmanager.fragments.AlarmLogFragment
import com.sensoguard.trailmanager.fragments.CameraCommandsDialogFragment
import com.sensoguard.trailmanager.fragments.CamerasFragment
import com.sensoguard.trailmanager.fragments.ConfigurationFragment
import com.sensoguard.trailmanager.fragments.MapSensorsFragment
import com.sensoguard.trailmanager.global.ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
import com.sensoguard.trailmanager.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.trailmanager.global.CURRENT_ITEM_TOP_MENU_KEY
import com.sensoguard.trailmanager.global.IS_MYSCREENACTIVITY_FOREGROUND
import com.sensoguard.trailmanager.global.MAIN_MENU_NUM_ITEM
import com.sensoguard.trailmanager.global.MAP_SHOW_SATELLITE_VALUE
import com.sensoguard.trailmanager.global.MAP_SHOW_VIEW_TYPE_KEY
import com.sensoguard.trailmanager.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.trailmanager.global.USB_CONNECTION_FAILED
import com.sensoguard.trailmanager.global.getIntInPreference
import com.sensoguard.trailmanager.global.getLongInPreference
import com.sensoguard.trailmanager.global.getStringInPreference
import com.sensoguard.trailmanager.global.setAppLanguage
import com.sensoguard.trailmanager.global.setBooleanInPreference
import com.sensoguard.trailmanager.global.setIntInPreference
import com.sensoguard.trailmanager.global.setLongInPreference
import com.sensoguard.trailmanager.global.setStringInPreference
import com.sensoguard.trailmanager.interfaces.OnFragmentListener


class MyScreensActivity : AppCompatActivity(), OnFragmentListener {


    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var collectionPagerAdapter: CollectionPagerAdapter
    private lateinit var viewPager: ViewPager
    private var currentItemTopMenu = 0
    //private var togChangeStatus: ToggleButton? = null


    val TAG = "MyScreensActivity"

    override fun onBackPressed() {
        //super.onBackPressed()
        val fragment = supportFragmentManager.findFragmentByTag("CameraCommandsDialogFragment")
        if (fragment != null && fragment.isVisible) {
            val res = (fragment as CameraCommandsDialogFragment).onBackPressed()
            if (!res) {
                return
            }
        }
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        //configurationLanguage()

        setContentView(R.layout.activity_my_screens)


        //store locally default values of configuration
        setConfigurationDefault()

        currentItemTopMenu = intent.getIntExtra(CURRENT_ITEM_TOP_MENU_KEY, 0)

        init()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            setExternalPermission()
//            //setLocationPermission()
//        } else {
//            init()
//        }

    }


    //store locally default values of configuration
    private fun setConfigurationDefault() {

        if (getLongInPreference(this, ALARM_FLICKERING_DURATION_KEY, -1L) == -1L) {
            //set the duration of flickering icon when accepted alarm
            setLongInPreference(this, ALARM_FLICKERING_DURATION_KEY, ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS)
        }

        if (getIntInPreference(this, MAP_SHOW_VIEW_TYPE_KEY, -1) == -1) {
            //set the type of ic_map_main
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(usbReceiver, filter)
        }
    }


    private fun init() {

        configureActionBar()

        //start listener to alarm
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(Intent(this, ServiceHandleAlarms::class.java))
//        } else {
//            startService(Intent(this, ServiceHandleAlarms::class.java))
//        }
        configTabs()

    }

//    @Override
//    protected override fun attachBaseContext(newBase:Context) {
//        configurationLanguage()
//    }


    private fun editActionBar(state: Boolean) {
        //togChangeStatus?.isChecked = state
    }

    //TODO : the toggle will updated by the status changing
    private fun configureActionBar() {

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)//supportActionBar
        setSupportActionBar(toolbar)

//        togChangeStatus = findViewById<ToggleButton>(
//            R.id.togChangeStatus
//        )
//        togChangeStatus?.isChecked = false
//
//        togChangeStatus?.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startForegroundService(Intent(this, ServiceConnectSensor::class.java))
//                } else {
//                    startService(Intent(this, ServiceConnectSensor::class.java))
//                }
//            } else {
//                sendBroadcast(Intent(STOP_READ_DATA_KEY))
//            }
//        }
    }

    override fun onPause() {
        super.onPause()
        setBooleanInPreference(this, IS_MYSCREENACTIVITY_FOREGROUND, false)
    }

    override fun onResume() {
        super.onResume()
        setBooleanInPreference(this, IS_MYSCREENACTIVITY_FOREGROUND, true)
    }

    private fun configTabs() {

        //val tabs = findViewById<TabLayout>(R.id.tab_layout)

        viewPager = findViewById(R.id.vPager)
        collectionPagerAdapter = CollectionPagerAdapter(supportFragmentManager)
        viewPager.adapter = collectionPagerAdapter
        viewPager.setOnTouchListener(object : OnTouchListener {

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return true
            }
        })

        //relate the tab layout to viewpager because we need to add the icons
//        tabs.setupWithViewPager(vPager)
//        tabs.getTabAt(0)?.icon = ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_sensor_tab)
//        tabs.getTabAt(1)?.icon = ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_map_tab)
//        tabs.getTabAt(2)?.icon = ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_config_tab)
//        tabs.getTabAt(3)?.icon = ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_alarm_log_tab)

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


    /**
     * callback of external permission
     */
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                init()
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }


    /**
     * check permission depend on sdk version
     */
    fun isGrantedPermissionWRITE_EXTERNAL_STORAGE(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT <= 32) {
            val isAllowPermissionApi28 = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            return isAllowPermissionApi28
        } else {
            val isAllowPermissionApi33 = Environment.isExternalStorageManager()
            return isAllowPermissionApi33
        }
    }

    /**
     * set external storage permission
     */
    private fun setExternalPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */

        val permission: String?
        if (Build.VERSION.SDK_INT <= 32) {
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            requestPermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            permission = Manifest.permission.READ_MEDIA_IMAGES
        }
        when {
            isGrantedPermissionWRITE_EXTERNAL_STORAGE(this) -> {
                // You can use the API that requires the permission.
                init()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                //showInContextUI(...)
            }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.

                if (Build.VERSION.SDK_INT <= 32) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                } else {
                    requestPermissionLauncher.launch(
                        Manifest.permission.READ_MEDIA_IMAGES

                    )
                }
            }
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
                    fragment = CamerasFragment()
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
                    fragment = AlarmLogFragment()
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
                0 -> resources.getString(R.string.cameras_title)
                1 -> resources.getString(R.string.map_title)
                2 -> resources.getString(R.string.config_title)
                3 -> resources.getString(R.string.alarm_log_title)
                else -> "nothing"
            }

        }

    }


    //set the language of the app (calling  from activity)
    override fun updateLanguage() {
        setAppLanguage(this, GeneralItemMenu.selectedItem)
        this.finish()
        this.startActivity(intent)
    }

}

