package com.sensoguard.trailmanager.activities

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.fragments.AlarmLogFragment
import com.sensoguard.trailmanager.fragments.CameraCommandsDialogFragment
import com.sensoguard.trailmanager.fragments.CamerasFragment
import com.sensoguard.trailmanager.fragments.ConfigurationFragment
import com.sensoguard.trailmanager.fragments.MapSensorsFragment
import com.sensoguard.trailmanager.global.ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
import com.sensoguard.trailmanager.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.trailmanager.global.CURRENT_ITEM_TOP_MENU_KEY
import com.sensoguard.trailmanager.global.IS_MY_SCREENS_ACTIVITY_FOREGROUND
import com.sensoguard.trailmanager.global.MAIN_MENU_NUM_ITEM
import com.sensoguard.trailmanager.global.MAP_SHOW_SATELLITE_VALUE
import com.sensoguard.trailmanager.global.MAP_SHOW_VIEW_TYPE_KEY
import com.sensoguard.trailmanager.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.trailmanager.global.applySystemBarsInsets
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

    private var currentItemTopMenu = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_my_screens)

        applySystemBarsInsets()

        //add back button
        setBackHandler()

        //store locally default values of configuration
        setConfigurationDefault()

        currentItemTopMenu = intent.getIntExtra(CURRENT_ITEM_TOP_MENU_KEY, 0)

        init()

        requestNotificationPermission()
    }

    /**
     * add back button
     */
    private fun setBackHandler() {
        //create the back button
        onBackPressedDispatcher.addCallback(
            this /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragment =
                        supportFragmentManager.findFragmentByTag("CameraCommandsDialogFragment")
                    if (fragment != null && fragment.isVisible) {
                        val res = (fragment as CameraCommandsDialogFragment).onBackPressed()
                        // res is false when the extra commands are shown: the back button returns to the previous list
                        // res is true when the main commands are shown: the back button dismisses the fragment
                        if (res) {
                            fragment.dismiss()
                            //remove also the entry of the back stack (the dialog was added with addToBackStack)
                            supportFragmentManager.popBackStack()
                        }
                    } else {// when CameraCommandsDialogFragment is not visible ,the back button is  close activity
                        finish()
                    }
                }
            })
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

            val uri = "android.resource://$packageName/raw/alarm_sound".toUri()

            setStringInPreference(this, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
        }
    }


    private fun init() {

        configureActionBar()

        configPages()

    }

    private fun configureActionBar() {

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val btnClearData: Button = toolbar.findViewById(R.id.btnClearData)
        btnClearData.setOnClickListener {
            showClearDataDialog()
        }
        setSupportActionBar(toolbar)

    }

    /**
     * clear data and cache
     */
    private fun clearAppData() {
        try {/* clearing app data */
            getSystemService(ActivityManager::class.java)?.clearApplicationUserData()
        } catch (e: Exception) {
            Log.w("MyScreensActivity", "clear data failed", e)
        }
    }


    /**
     * show dialog before clear data
     */
    private fun showClearDataDialog() {
        AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.clear_data))
            .setMessage(resources.getString(R.string.do_you_really_want_clear_data))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                clearAppData()
                dialog.dismiss()
            }
            // Display a negative button on alert dialog
            .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onPause() {
        super.onPause()
        setBooleanInPreference(this, IS_MY_SCREENS_ACTIVITY_FOREGROUND, false)
    }

    override fun onResume() {
        super.onResume()
        setBooleanInPreference(this, IS_MY_SCREENS_ACTIVITY_FOREGROUND, true)
    }

    private fun configPages() {
        val viewPager = findViewById<ViewPager2>(R.id.vPager)
        viewPager.adapter = CollectionPagerAdapter(this)
        //the pages are changed from code only (no tabs, no swipe)
        viewPager.isUserInputEnabled = false
        //keep the next page ready (as the previous ViewPager did)
        viewPager.offscreenPageLimit = 1
        viewPager.setCurrentItem(currentItemTopMenu, false)
    }


    /**
     * callback of notifications permission (Android 13+)
     */
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    /**
     * since Android 13 the alarm notifications are not shown without POST_NOTIFICATIONS permission
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    //the main screens: cameras, map, configuration and alarm log
    private class CollectionPagerAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = MAIN_MENU_NUM_ITEM

        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> CamerasFragment()
                1 -> MapSensorsFragment()
                2 -> ConfigurationFragment()
                else -> AlarmLogFragment()
            }
            fragment.arguments = Bundle().apply {
                putInt("ARG_OBJECT", position + 1)
            }
            return fragment
        }
    }


    //set the language of the app (calling  from activity)
    override fun updateLanguage() {
        setAppLanguage(this)
        this.finish()
        this.startActivity(intent)
    }

}
