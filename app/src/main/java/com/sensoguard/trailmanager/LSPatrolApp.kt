package com.sensoguard.trailmanager

import android.app.Application
import com.sensoguard.trailmanager.classes.GeneralItemMenu
import com.sensoguard.trailmanager.classes.LanguageManager
import com.sensoguard.trailmanager.global.CURRENT_LANG_KEY_PREF
import com.sensoguard.trailmanager.global.getAppLanguage
import com.sensoguard.trailmanager.global.getStringInPreference
import com.sensoguard.trailmanager.global.setAppLanguage

class LSPatrolApp : Application() {

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
}