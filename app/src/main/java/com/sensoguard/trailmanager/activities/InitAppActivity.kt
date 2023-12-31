package com.sensoguard.trailmanager.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.CryptoHandler
import com.sensoguard.trailmanager.classes.GeneralItemMenu
import com.sensoguard.trailmanager.classes.LanguageManager
import com.sensoguard.trailmanager.global.ACTIVATION_CODE_KEY
import com.sensoguard.trailmanager.global.CURRENT_ITEM_TOP_MENU_KEY
import com.sensoguard.trailmanager.global.CURRENT_LANG_KEY_PREF
import com.sensoguard.trailmanager.global.IMEI_KEY
import com.sensoguard.trailmanager.global.NO_DATA
import com.sensoguard.trailmanager.global.PERMISSIONS_REQUEST_READ_PHONE_STATE
import com.sensoguard.trailmanager.global.getAppLanguage
import com.sensoguard.trailmanager.global.getStringInPreference
import com.sensoguard.trailmanager.global.setAppLanguage

class InitAppActivity : AppCompatActivity() {

    private var myImei: String?=null

   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_init_app)
       setReadPhoneStatePermission()
   }

    //get the IMEI of the device and check it with the locally
    private fun configureActivation(){

        myImei=getDeviceIMEI()

        val localActivateCode= getStringInPreference(this, ACTIVATION_CODE_KEY, NO_DATA)
        if(!localActivateCode.equals(NO_DATA)){
            val myActivateCode= CryptoHandler.getInstance().encrypt(myImei)
            if(localActivateCode!=null && myActivateCode.startsWith(localActivateCode)){
                openCameraScreen()
//                val inn = Intent(this, MainActivity::class.java)
//                inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                startActivity(inn)
            }else{
                Toast.makeText(this,resources.getString(R.string.wrong_activate_code), Toast.LENGTH_SHORT).show()
                openActivation()
            }
        }else{
            openActivation()
        }
    }

    //in trail manager ,open camera screen immediately
    private fun openCameraScreen() {
        val inn = Intent(this, MyScreensActivity::class.java)
        inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY, 0)
        inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(inn)
    }

    //open activation screen
    private fun openActivation() {
        val inn = Intent(this, ActivationActivity::class.java)
        inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        inn.putExtra(IMEI_KEY,myImei)
        startActivity(Intent(inn))
    }


    //get the device IMEI
    private fun getDeviceIMEI(): String? {
        var deviceUniqueIdentifier: String? = null
        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (null != tm) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                deviceUniqueIdentifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tm.imei
                } else {
                    tm.deviceId
                }
            }
        }
        if (null == deviceUniqueIdentifier || deviceUniqueIdentifier.isEmpty()) {
            deviceUniqueIdentifier =
                Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        }
        return deviceUniqueIdentifier
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_PHONE_STATE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    configurationLanguage()
                    configureActivation()
                }
            }
        }

    }

    //set permission of READ_PHONE_STATE
    private fun setReadPhoneStatePermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            configurationLanguage()
            configureActivation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE), PERMISSIONS_REQUEST_READ_PHONE_STATE
            )
        }
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
                //GeneralItemMenu.selectedItem = "iw"
                setAppLanguage(this, GeneralItemMenu.selectedItem)
            }
        }
    }
}
