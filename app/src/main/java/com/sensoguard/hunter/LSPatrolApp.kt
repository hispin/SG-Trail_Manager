package com.sensoguard.hunter

import android.app.Application
import com.sensoguard.hunter.classes.GeneralItemMenu
import com.sensoguard.hunter.classes.LanguageManager
import com.sensoguard.hunter.global.CURRENT_LANG_KEY_PREF
import com.sensoguard.hunter.global.getAppLanguage
import com.sensoguard.hunter.global.getStringInPreference
import com.sensoguard.hunter.global.setAppLanguage

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