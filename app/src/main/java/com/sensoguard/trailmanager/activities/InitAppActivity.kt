package com.sensoguard.trailmanager.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.CryptoHandler
import com.sensoguard.trailmanager.classes.GeneralItemMenu
import com.sensoguard.trailmanager.classes.LanguageManager
import com.sensoguard.trailmanager.global.ACTIVATION_CODE_KEY
import com.sensoguard.trailmanager.global.CURRENT_ITEM_TOP_MENU_KEY
import com.sensoguard.trailmanager.global.CURRENT_LANG_KEY_PREF
import com.sensoguard.trailmanager.global.IMEI_KEY
import com.sensoguard.trailmanager.global.NO_DATA
import com.sensoguard.trailmanager.global.applySystemBarsInsets
import com.sensoguard.trailmanager.global.getAppLanguage
import com.sensoguard.trailmanager.global.getStringInPreference
import com.sensoguard.trailmanager.global.setAppLanguage

class InitAppActivity : AppCompatActivity() {

    private var myImei: String?=null

   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_init_app)
       applySystemBarsInsets()
       configurationLanguage()
       configureActivation()
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
        startActivity(inn)
    }


    //get the unique id of the device.
    //The activation code is calculated from this value, so it must stay stable:
    //the IMEI is not accessible since Android 10, and in practice the app always used ANDROID_ID
    //(the id is used only for the license of this app, not for ads / tracking)
    @SuppressLint("HardwareIds")
    private fun getDeviceIMEI(): String? {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun configurationLanguage() {
        LanguageManager.setLanguageList()
        val currentLanguage = getStringInPreference(this, CURRENT_LANG_KEY_PREF, "-1")
        if (currentLanguage != "-1") {
            GeneralItemMenu.selectedItem = currentLanguage
            setAppLanguage(this)
        } else {
            val deviceLang = getAppLanguage()
            if (LanguageManager.isExistLang(deviceLang)) {
                //GeneralItemMenu.selectedItem = "iw"
                setAppLanguage(this)
            }
        }
    }
}
