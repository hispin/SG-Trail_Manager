package com.sensoguard.trailmanager.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListPopupWindow
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.adapters.GeneralItemMenuAdapter
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.classes.GeneralItemMenu
import com.sensoguard.trailmanager.classes.LanguageManager
import com.sensoguard.trailmanager.classes.MyEmailAccount
import com.sensoguard.trailmanager.global.ALARM_DISPLAY_KEY
import com.sensoguard.trailmanager.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.trailmanager.global.CAMERA_KEY
import com.sensoguard.trailmanager.global.CURRENT_LANG_KEY_PREF
import com.sensoguard.trailmanager.global.EMAIL_ACCOUNT_KEY
import com.sensoguard.trailmanager.global.ERROR_RESULT_VALIDATION_EMAIL_ACTION
import com.sensoguard.trailmanager.global.ERROR_VALIDATION_EMAIL_MSG_KEY
import com.sensoguard.trailmanager.global.IS_EMAIL_CONFIG_PREF_KEY
import com.sensoguard.trailmanager.global.IS_NOTIFICATION_SOUND_KEY
import com.sensoguard.trailmanager.global.IS_VIBRATE_WHEN_ALARM_KEY
import com.sensoguard.trailmanager.global.LAST_DATE_ALARM
import com.sensoguard.trailmanager.global.MAP_SHOW_NORMAL_VALUE
import com.sensoguard.trailmanager.global.MAP_SHOW_SATELLITE_VALUE
import com.sensoguard.trailmanager.global.MAP_SHOW_VIEW_TYPE_KEY
import com.sensoguard.trailmanager.global.RESULT_VALIDATION_EMAIL_ACTION
import com.sensoguard.trailmanager.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.trailmanager.global.VALIDATION_EMAIL_RESULT
import com.sensoguard.trailmanager.global.convertToGson
import com.sensoguard.trailmanager.global.getBooleanInPreference
import com.sensoguard.trailmanager.global.getCamerasFromLocally
import com.sensoguard.trailmanager.global.getIntInPreference
import com.sensoguard.trailmanager.global.getLongInPreference
import com.sensoguard.trailmanager.global.getMyEmailAccountFromLocally
import com.sensoguard.trailmanager.global.getScreenWidth
import com.sensoguard.trailmanager.global.getStringInPreference
import com.sensoguard.trailmanager.global.removePreference
import com.sensoguard.trailmanager.global.setBooleanInPreference
import com.sensoguard.trailmanager.global.setIntInPreference
import com.sensoguard.trailmanager.global.setLongInPreference
import com.sensoguard.trailmanager.global.setStringInPreference
import com.sensoguard.trailmanager.global.storeMyEmailAccountLocally
import com.sensoguard.trailmanager.global.storeSensorsToLocally
import com.sensoguard.trailmanager.interfaces.CallToParentInterface
import com.sensoguard.trailmanager.interfaces.OnFragmentListener
import com.sensoguard.trailmanager.services.ServiceEmailValidation
import java.util.Locale

//the maximum number of sensors
private const val MAX_SENSORS = 254

/**
 * The configuration of the app (sensors, alarm sound, map, language, email account)
 */
class ConfigurationFragment : Fragment(), CallToParentInterface {


    private var myEmailAccount: MyEmailAccount? = null
    private var listPopupWindow: ListPopupWindow? = null
    private var etSensorValue: AppCompatEditText? = null
    private var btnSaveSensors: AppCompatButton? = null
    private var togChangeAlarmVibrate: ToggleButton? = null
    private var ibSatelliteMode: AppCompatButton? = null
    private var ibNormalMode: AppCompatButton? = null
    private var etAlarmFlickerValue: AppCompatEditText? = null
    private var btnSaveFlicker: AppCompatButton? = null
    private var constAlarmSound: ConstraintLayout? = null
    private var txtAlarmSoundValue: TextView? = null
    private var togChangeAlarmSound: ToggleButton? = null
    private var btnDefault: AppCompatButton? = null
    private var constLangView: ConstraintLayout? = null
    private var languageValue: TextView? = null
    private var listener: OnFragmentListener? = null
    private var rgAlarmDisplay: RadioGroup? = null
    private var etMailAddress: AppCompatEditText? = null
    private var etPassword: AppCompatEditText? = null
    private var etMailServer: AppCompatEditText? = null
    private var etMailServerPort: AppCompatEditText? = null
    private var rgIsSSL: RadioGroup? = null
    private var pbValidationEmail: ProgressBar? = null
    private var btnSave: AppCompatButton? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_configuration,
            container,
            false
        )

        etSensorValue = view.findViewById(R.id.etSensorValue)
        getCurrentNumSensorsFromLocally()?.let {
            etSensorValue?.setText(String.format(Locale.getDefault(), "%d", it))
        }

        btnSaveSensors = view.findViewById(R.id.btnSaveSensors)
        btnSaveSensors?.setOnClickListener {
            addSensors()
        }

        togChangeAlarmVibrate = view.findViewById(R.id.togChangeAlarmVibrate)
        togChangeAlarmVibrate?.isChecked =
            getBooleanInPreference(activity, IS_VIBRATE_WHEN_ALARM_KEY, true)
        togChangeAlarmVibrate?.setOnCheckedChangeListener { _, isChecked ->
            //update the status of the alarm vibrate : on/off
            setBooleanInPreference(activity, IS_VIBRATE_WHEN_ALARM_KEY, isChecked)
        }

        ibSatelliteMode = view.findViewById(R.id.ibSatelliteMode)
        ibSatelliteMode?.setOnClickListener {
            setMapSatellite()
        }

        ibNormalMode = view.findViewById(R.id.ibNormalMode)
        ibNormalMode?.setOnClickListener {
            setMapNormal()
        }
        when (getIntInPreference(activity, MAP_SHOW_VIEW_TYPE_KEY, -1)) {
            MAP_SHOW_NORMAL_VALUE -> setMapNormal()
            MAP_SHOW_SATELLITE_VALUE -> setMapSatellite()
        }

        etAlarmFlickerValue = view.findViewById(R.id.etAlarmFlickerValue)
        etAlarmFlickerValue?.setText(
            String.format(
                Locale.getDefault(),
                "%d",
                getLongInPreference(activity, ALARM_FLICKERING_DURATION_KEY, -1L)
            )
        )

        btnSaveFlicker = view.findViewById(R.id.btnSaveFlicker)
        //update the time flickering
        btnSaveFlicker?.setOnClickListener {
            val timeFlicker =
                etAlarmFlickerValue?.text.toString().toLongOrNull() ?: return@setOnClickListener
            setLongInPreference(activity, ALARM_FLICKERING_DURATION_KEY, timeFlicker)
            Toast.makeText(
                activity,
                resources.getString(R.string.time_flickering_save_successfully),
                Toast.LENGTH_SHORT
            ).show()
        }

        constAlarmSound = view.findViewById(R.id.constAlarmSound)
        constAlarmSound?.setOnClickListener {
            openSoundsMenu()
        }
        txtAlarmSoundValue = view.findViewById(R.id.txtAlarmSoundValue)
        txtAlarmSoundValue?.text = getSelectedNotificationSound()

        togChangeAlarmSound = view.findViewById(R.id.togChangeAlarmSound)
        togChangeAlarmSound?.isChecked =
            getBooleanInPreference(activity, IS_NOTIFICATION_SOUND_KEY, true)
        togChangeAlarmSound?.setOnCheckedChangeListener { _, isChecked ->
            //update the status of the alarm sound : on/off
            setBooleanInPreference(activity, IS_NOTIFICATION_SOUND_KEY, isChecked)
        }

        btnDefault = view.findViewById(R.id.btnDefault)
        btnDefault?.setOnClickListener {
            //return the alarm to default sound
            val uri = "android.resource://${activity?.packageName}/raw/alarm_sound".toUri()
            setStringInPreference(activity, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
            txtAlarmSoundValue?.text = getSelectedNotificationSound()
        }

        constLangView = view.findViewById(R.id.constLangView)
        constLangView?.setOnClickListener {
            showPopupList(it)
        }

        languageValue = view.findViewById(R.id.languageValue)


        rgAlarmDisplay = view.findViewById(R.id.rgAlarmDisplay)

        rgAlarmDisplay?.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbListView -> setStringInPreference(this.context, ALARM_DISPLAY_KEY, "list")
                R.id.rbGridView -> setStringInPreference(this.context, ALARM_DISPLAY_KEY, "grid")
            }
        }

        etMailServer = view.findViewById(R.id.etMailServer)
        etMailServerPort = view.findViewById(R.id.etMailServerPort)
        etMailAddress = view.findViewById(R.id.etMailAddress)
        rgIsSSL = view.findViewById(R.id.rgIsSSL)
        //add automatically when set address as gmail
        etMailAddress?.setOnFocusChangeListener { emailView, hasFocus ->
            if (!hasFocus && (emailView as EditText).text.toString().contains("@gmail.com")) {
                //fill automatically
                etMailServer?.setError("", null)
                etMailServer?.setText(R.string.gmail_imap_server)
                etMailServerPort?.setError("", null)
                etMailServerPort?.setText(R.string.gmail_imap_port)
                rgIsSSL?.check(R.id.rbYes)
            }
        }
        etPassword = view.findViewById(R.id.etPassword)
        pbValidationEmail = view.findViewById(R.id.pbValidationEmail)

        btnSave = view.findViewById(R.id.btnSave)
        btnSave?.setOnClickListener {
            //validate all the fields (to show the error on each empty field)
            val isServerValid = validIsEmpty(etMailServer)
            val isPortValid = validIsEmpty(etMailServerPort)
            val isAddressValid = validIsEmpty(etMailAddress)
            val isPasswordValid = validIsEmpty(etPassword)
            if (isServerValid && isPortValid && isAddressValid && isPasswordValid) {
                myEmailAccount = MyEmailAccount(
                    etMailAddress?.text.toString(),
                    etPassword?.text.toString(),
                    etMailServer?.text.toString(),
                    etMailServerPort?.text.toString(),
                    rgIsSSL?.checkedRadioButtonId == R.id.rbYes
                )
                pbValidationEmail?.visibility = View.VISIBLE
                startServiceEmailValidation()
            } else {
                //set false the flag of email configuration
                setBooleanInPreference(activity, IS_EMAIL_CONFIG_PREF_KEY, false)
            }
        }

        setMyEmailAccountFields()

        return view
    }

    private fun setMyEmailAccountFields() {
        val context = activity ?: return
        val emailAccount = getMyEmailAccountFromLocally(context) ?: return
        myEmailAccount = emailAccount
        etMailAddress?.setText(emailAccount.emailAddress)
        etMailServer?.setText(emailAccount.emailServer)
        etMailServerPort?.setText(emailAccount.emailPort)
        etPassword?.setText(emailAccount.password)
        rgIsSSL?.check(if (emailAccount.isUseSSL) R.id.rbYes else R.id.rbNo)
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(RESULT_VALIDATION_EMAIL_ACTION)
        filter.addAction(ERROR_RESULT_VALIDATION_EMAIL_ACTION)
        activity?.let {
            ContextCompat.registerReceiver(
                it,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        //set the current language (or selected language or language of device)
        showCurrentLanguage(LanguageManager.getCurrentLang(GeneralItemMenu.selectedItem))

        when (getStringInPreference(this.context, ALARM_DISPLAY_KEY, "list")) {
            "list" -> rgAlarmDisplay?.check(R.id.rbListView)
            "grid" -> rgAlarmDisplay?.check(R.id.rbGridView)
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(receiver)
    }

    //get selected notification sound from locally
    private fun getSelectedNotificationSound(): String? {
        val selectedSound = getStringInPreference(activity, SELECTED_NOTIFICATION_SOUND_KEY, "-1")
        if (selectedSound == null || selectedSound == "-1") {
            return null
        }
        return RingtoneManager.getRingtone(activity, selectedSound.toUri())?.getTitle(activity)
    }

    //open menu of notification sounds
    private fun openSoundsMenu() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        intent.putExtra(
            RingtoneManager.EXTRA_RINGTONE_TITLE,
            getString(R.string.select_alarm_sound)
        )
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
        soundPickerLauncher.launch(intent)
    }

    //callback of the notification sounds menu
    private val soundPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onSoundPicked(result.resultCode, result.data)
        }

    private fun onSoundPicked(resultCode: Int, intent: Intent?) {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return
        }
        val uri = IntentCompat.getParcelableExtra(
            intent,
            RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
            Uri::class.java
        )
        if (uri != null) {
            txtAlarmSoundValue?.text =
                RingtoneManager.getRingtone(activity, uri)?.getTitle(activity)
            setStringInPreference(activity, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
        } else {
            txtAlarmSoundValue?.text = resources.getString(R.string.no_selected_sound)
        }
    }

    private fun setMapSatellite() {
        ibNormalMode?.isEnabled = true
        ibSatelliteMode?.isEnabled = false
        setIntInPreference(activity, MAP_SHOW_VIEW_TYPE_KEY, MAP_SHOW_SATELLITE_VALUE)
    }


    private fun setMapNormal() {
        ibNormalMode?.isEnabled = false
        ibSatelliteMode?.isEnabled = true
        setIntInPreference(activity, MAP_SHOW_VIEW_TYPE_KEY, MAP_SHOW_NORMAL_VALUE)
    }

    //get the current size of sensors
    private fun getCurrentNumSensorsFromLocally(): Int? {
        return activity?.let { getCamerasFromLocally(it) }?.size
    }


    //ask before delete the sensors with an id bigger than the requested number
    private fun askBeforeDeleteExtraSensor(sensors: ArrayList<Camera>, numSensorsRequest: Int) {
        AlertDialog.Builder(activity)
            //set message, title, and icon
            .setTitle(getString(R.string.remove_extra_sensors))
            .setMessage(getString(R.string.content_delete_extra_sensor))
            .setIcon(android.R.drawable.ic_menu_delete)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->

                //remove extra sensors (an invalid id is kept)
                sensors.removeAll { (it.getId().toIntOrNull() ?: 0) > numSensorsRequest }
                Toast.makeText(
                    activity,
                    resources.getString(R.string.sensors_save_successfully),
                    Toast.LENGTH_SHORT
                ).show()

                activity?.let { storeSensorsToLocally(sensors, it) }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    //add sensors according to the number that get from user
    private fun addSensors() {
        val context = activity ?: return
        val sensors = getCamerasFromLocally(context) ?: return

        val numSensorsRequest = etSensorValue?.text.toString().toIntOrNull()
        if (numSensorsRequest == null) {
            Toast.makeText(context, resources.getString(R.string.error), Toast.LENGTH_LONG).show()
            return
        }

        if (numSensorsRequest > MAX_SENSORS) {
            Toast.makeText(
                context,
                resources.getString(R.string.invalid_mum_sensors),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        //remove last date and enable scan last date
        removePreference(context, LAST_DATE_ALARM)

        //add numSensors sensors (just if not exist)
        for (sensorId in 1..numSensorsRequest) {
            if (sensors.none { it.getId() == sensorId.toString() }) {
                sensors.add(Camera(sensorId.toString()))
            }
        }

        //check if the requested number of sensors is smaller than the existing number
        if (numSensorsRequest < sensors.size) {
            askBeforeDeleteExtraSensor(sensors, numSensorsRequest)
        } else {
            storeSensorsToLocally(sensors, context)
            Toast.makeText(
                context,
                resources.getString(R.string.sensors_save_successfully),
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun showPopupList(anchorView: View) {
        val context = context ?: return
        val languagesItems = LanguageManager.languagesItems
        if (languagesItems.isNullOrEmpty()) {
            Toast.makeText(
                activity,
                resources.getString(R.string.error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        //a copy: to prevent two identical list with the same address and depends on each other
        val generalItemMenuAdapter = GeneralItemMenuAdapter(
            context,
            R.layout.item_general_menu,
            ArrayList(languagesItems),
            this
        )
        val popup = ListPopupWindow(context)
        listPopupWindow = popup
        popup.isModal = true
        popup.animationStyle = R.style.winPopupAnimation
        popup.setAdapter(generalItemMenuAdapter)
        popup.anchorView = anchorView
        popup.width = getScreenWidth(activity) * 2 / 3
        popup.show()
    }

    //select language for app
    override fun selectedItem(item: Any) {
        setStringInPreference(
            activity,
            CURRENT_LANG_KEY_PREF,
            GeneralItemMenu.selectedItem
        )
        if (item is GeneralItemMenu && listPopupWindow?.isShowing == true) {
            listPopupWindow?.dismiss()
            showCurrentLanguage(item)
            listener?.updateLanguage()
        }
    }

    //show current language in menu
    private fun showCurrentLanguage(generalItemMenu: GeneralItemMenu?) {
        generalItemMenu?.let { languageValue?.text = it.title }
    }

    //check if the fields is empty
    private fun validIsEmpty(editText: EditText?): Boolean {
        if (editText == null) {
            return false
        }
        if (editText.text.isNullOrBlank()) {
            editText.error = resources.getString(R.string.empty_field_error)
            return false
        }
        return true
    }

    //start email validation
    private fun startServiceEmailValidation() {
        val context = context ?: return
        val serviceIntent = Intent(context, ServiceEmailValidation::class.java)
        myEmailAccount?.let { serviceIntent.putExtra(CAMERA_KEY, convertToGson(it)) }
        ContextCompat.startForegroundService(context, serviceIntent)
    }


    //handle broadcast
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == RESULT_VALIDATION_EMAIL_ACTION) {
                pbValidationEmail?.visibility = View.GONE
                val resultValidationEmail = intent.getBooleanExtra(VALIDATION_EMAIL_RESULT, false)
                if (resultValidationEmail) {

                    //set true the flag of email configuration
                    setBooleanInPreference(activity, IS_EMAIL_CONFIG_PREF_KEY, true)

                    //remove last date and enable scan last date
                    removePreference(activity, LAST_DATE_ALARM)

                    Toast.makeText(
                        activity,
                        resources.getString(R.string.validation_successfully),
                        Toast.LENGTH_LONG
                    ).show()
                    //store the email account locally
                    val emailAccount = myEmailAccount
                    val activity = activity
                    if (emailAccount != null && activity != null) {
                        storeMyEmailAccountLocally(emailAccount, activity)
                    }
                } else {
                    //set false the flag of email configuration
                    setBooleanInPreference(activity, IS_EMAIL_CONFIG_PREF_KEY, false)

                    //remove email account locally
                    removePreference(context, EMAIL_ACCOUNT_KEY)
                }
            } else if (intent?.action == ERROR_RESULT_VALIDATION_EMAIL_ACTION) {
                pbValidationEmail?.visibility = View.GONE
                val errorMsg = intent.getStringExtra(ERROR_VALIDATION_EMAIL_MSG_KEY)
                errorMsg?.let { Toast.makeText(activity, it, Toast.LENGTH_LONG).show() }
                //set false the flag of email configuration
                setBooleanInPreference(activity, IS_EMAIL_CONFIG_PREF_KEY, false)
            }
        }

    }


}
