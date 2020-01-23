package com.sensoguard.hunter.fragments

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
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sensoguard.hunter.R
import com.sensoguard.hunter.adapters.GeneralItemMenuAdapter
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.classes.GeneralItemMenu
import com.sensoguard.hunter.classes.LanguageManager
import com.sensoguard.hunter.classes.MyEmailAccount
import com.sensoguard.hunter.global.*
import com.sensoguard.hunter.interfaces.CallToParentInterface
import com.sensoguard.hunter.interfaces.OnFragmentListener
import com.sensoguard.hunter.services.ServiceEmailValidation


open class ConfigurationFragment : Fragment(),CallToParentInterface{


    private var myEmailAccount: MyEmailAccount? = null
    private var listPopupWindow: ListPopupWindow?=null
    private var generalItemMenuAdapter: GeneralItemMenuAdapter?=null
    private var etSensorValue: AppCompatEditText?=null
    private var btnSaveSensors: AppCompatButton?=null
    private var togChangeAlarmVibrate: ToggleButton?=null
    private var ibSatelliteMode:AppCompatButton?=null
    private var ibNormalMode:AppCompatButton?=null
    private var etAlarmFlickerValue:AppCompatEditText?=null
    private var btnSaveFlicker:AppCompatButton?=null
    private var constAlarmSound:ConstraintLayout?=null
    private var txtAlarmSoundValue:TextView?=null
    private var togChangeAlarmSound:ToggleButton?=null
    private var btnDefault: AppCompatButton?=null
    //private var ivSelectLanguage: AppCompatImageView?=null
    private var constLangView:ConstraintLayout?=null
    private var languageValue:TextView?=null
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
            throw RuntimeException("$context must implement OnAdapterListener")
        }
    }

    //(activity,ALARM_FLICKERING_DURATION_KEY,ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            com.sensoguard.hunter.R.layout.fragment_configuration,
            container,
            false
        )

        etSensorValue = view.findViewById(com.sensoguard.hunter.R.id.etSensorValue)
        val currentNumSensors=getCurrentNumSensorsFromLocally()
        if(currentNumSensors!=null) {
            etSensorValue?.setText(currentNumSensors.toString())
        }

        btnSaveSensors = view.findViewById(com.sensoguard.hunter.R.id.btnSaveSensors)
        btnSaveSensors?.setOnClickListener{
            addSensors()
        }

        togChangeAlarmVibrate = view.findViewById(com.sensoguard.hunter.R.id.togChangeAlarmVibrate)
        togChangeAlarmVibrate?.isChecked=getBooleanInPreference(activity,IS_VIBRATE_WHEN_ALARM_KEY,true)
        togChangeAlarmVibrate?.setOnCheckedChangeListener { buttonView, isChecked ->
            //update the status of the alarm vibrate : on/off
            setBooleanInPreference(activity,IS_VIBRATE_WHEN_ALARM_KEY,isChecked)
        }

        ibSatelliteMode = view.findViewById(com.sensoguard.hunter.R.id.ibSatelliteMode)
        ibSatelliteMode?.setOnClickListener{
            setMapSatellite()
        }

        ibNormalMode = view.findViewById(com.sensoguard.hunter.R.id.ibNormalMode)
        ibNormalMode?.setOnClickListener{
            setMapNormal()
        }
        val mapType = getIntInPreference(activity,MAP_SHOW_VIEW_TYPE_KEY,-1)
        if(mapType==MAP_SHOW_NORMAL_VALUE){
            setMapNormal()
        }else if(mapType== MAP_SHOW_SATELLITE_VALUE){
            setMapSatellite()
        }

        etAlarmFlickerValue = view.findViewById(com.sensoguard.hunter.R.id.etAlarmFlickerValue)
        etAlarmFlickerValue?.setText(getLongInPreference(activity,ALARM_FLICKERING_DURATION_KEY,-1L).toString())

        btnSaveFlicker = view.findViewById(com.sensoguard.hunter.R.id.btnSaveFlicker)
        //update the time flickering
        btnSaveFlicker?.setOnClickListener{
            try {
                val timeFlicker=etAlarmFlickerValue?.text.toString().toLong()
                setLongInPreference(activity,ALARM_FLICKERING_DURATION_KEY,timeFlicker)
                Toast.makeText(
                    activity,
                    resources.getString(com.sensoguard.hunter.R.string.time_flickering_save_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            }catch (ex:NumberFormatException){}

        }

        constAlarmSound = view.findViewById(com.sensoguard.hunter.R.id.constAlarmSound)
        constAlarmSound?.setOnClickListener{
            openSoundsMenu()
        }
        txtAlarmSoundValue = view.findViewById(com.sensoguard.hunter.R.id.txtAlarmSoundValue)

        var title=getSelectedNotificationSound()
        txtAlarmSoundValue?.text=title

        togChangeAlarmSound = view.findViewById(com.sensoguard.hunter.R.id.togChangeAlarmSound)
        togChangeAlarmSound?.isChecked= getBooleanInPreference(activity,IS_NOTIFICATION_SOUND_KEY,true)
        togChangeAlarmSound?.setOnCheckedChangeListener { buttonView, isChecked ->
            //update the status of the alarm vibrate : on/off
            setBooleanInPreference(activity,IS_NOTIFICATION_SOUND_KEY,isChecked)
        }

        btnDefault = view.findViewById(com.sensoguard.hunter.R.id.btnDefault)
        btnDefault?.setOnClickListener{
            //return the alarm to default sound
            val packageName = "android.resource://${activity?.packageName}/raw/alarm_sound"
            val uri=Uri.parse(packageName)
            setStringInPreference(activity, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
            title=getSelectedNotificationSound()
            txtAlarmSoundValue?.text=title
        }

//        ivSelectLanguage=view.findViewById(R.id.ivSelectLanguage)
//        ivSelectLanguage?.setOnClickListener{
//            showPopupList(it)
//        }

        constLangView=view.findViewById(R.id.constLangView)
        constLangView?.setOnClickListener{
            showPopupList(it)
        }

        languageValue=view.findViewById(R.id.languageValue)


        rgAlarmDisplay = view.findViewById(R.id.rgAlarmDisplay)

        rgAlarmDisplay?.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rbListView) {
                setStringInPreference(this.context, ALARM_DISPLAY_KEY, "list")
            } else if (checkedId == R.id.rbGridView) {
                setStringInPreference(this.context, ALARM_DISPLAY_KEY, "grid")
            }
        }

        etMailServer = view.findViewById(R.id.etMailServer)
        etMailServerPort = view?.findViewById(R.id.etMailServerPort)
        etMailAddress = view?.findViewById(R.id.etMailAddress)
        //add automatically when set address as gmail
        etMailAddress?.setOnFocusChangeListener { view, hasFocus ->

            if (!hasFocus) {
                if ((view as EditText).text.toString().contains("@gmail.com")) {
                    //fill automatically
                    etMailServer?.setError("", null)
                    etMailServer?.setText("imap.gmail.com")
                    etMailServerPort?.setError("", null)
                    etMailServerPort?.setText("993")
                    rgIsSSL?.check(R.id.rbYes)

                }
            }
        }
        rgIsSSL = view?.findViewById(R.id.rgIsSSL)
        etPassword = view?.findViewById(R.id.etPassword)
        pbValidationEmail = view?.findViewById(R.id.pbValidationEmail)

        btnSave = view?.findViewById(R.id.btnSave)
        btnSave?.setOnClickListener {
            if (validIsEmpty(etMailServer!!)
                and validIsEmpty(etMailServerPort!!)
                and validIsEmpty(etMailAddress!!)
                and validIsEmpty(etPassword!!)
            ) {
                myEmailAccount = MyEmailAccount(
                    etMailAddress?.text.toString()
                    , etPassword?.text.toString()
                    , etMailServer?.text.toString()
                    , etMailServerPort?.text.toString()
                    , rgIsSSL?.checkedRadioButtonId == R.id.rbYes
                )
                pbValidationEmail?.visibility = View.VISIBLE
                startServiceEmailValidation()

            }
        }

        setMyEmailAccountFields()

        return view
    }

    private fun setMyEmailAccountFields() {
        val myEmailAccountStr = getStringInPreference(activity, EMAIL_ACCOUNT_KEY, null)
        myEmailAccountStr?.let {
            myEmailAccount = convertJsonToMyEmailAccount(myEmailAccountStr)
            if (myEmailAccount != null) {
                etMailAddress?.setText(myEmailAccount?.emailAddress)
                etMailServer?.setText(myEmailAccount?.emailServer)
                etMailServerPort?.setText(myEmailAccount?.emailPort)
                etPassword?.setText(myEmailAccount?.password)
                if (myEmailAccount?.isUseSSL != null && myEmailAccount?.isUseSSL!!) {
                    rgIsSSL?.check(R.id.rbYes)
                } else {
                    rgIsSSL?.check(R.id.rbNo)
                }
            }


        }
    }

    override fun onStart() {
        super.onStart()
        setFilter()
        //set the current language (or selected language or language of device)
        val generalItemMenu = LanguageManager.getCurrentLang(GeneralItemMenu.selectedItem)
        if (generalItemMenu != null) {
            showCurrentLanguage(generalItemMenu)
        }

        val alarmDisplay = getStringInPreference(this.context, ALARM_DISPLAY_KEY, "list")
        if (alarmDisplay != null && (alarmDisplay == "list")) {
            rgAlarmDisplay?.check(R.id.rbListView)
        } else if (alarmDisplay != null && (alarmDisplay == "grid")) {
            rgAlarmDisplay?.check(R.id.rbGridView)
        }
    }

    //get selected notification sound from locally
    private fun getSelectedNotificationSound(): String? {
        val selectedSound =getStringInPreference(activity,SELECTED_NOTIFICATION_SOUND_KEY,"-1")
        if(!selectedSound.equals("-1")) {
            val uri=Uri.parse(selectedSound)
            uri?.let {
                val ringtone = RingtoneManager.getRingtone(activity, uri)
                return ringtone.getTitle(activity)
            }
        }
        return null
    }

    //open menu of notification sounds
    private fun openSoundsMenu(){
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm")
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
        this.startActivityForResult(intent, 5)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            val uri = intent!!.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

            if (uri != null) {
                val ringtone = RingtoneManager.getRingtone(activity, uri)
                val title = ringtone.getTitle(activity)
                txtAlarmSoundValue?.text = title
                setStringInPreference(activity, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
            } else {
                txtAlarmSoundValue?.text =
                    resources.getString(com.sensoguard.hunter.R.string.no_selected_sound)
            }
        }
    }

    private fun setMapSatellite() {
        ibNormalMode?.isEnabled = true
        ibSatelliteMode?.isEnabled = false
        setIntInPreference(activity,MAP_SHOW_VIEW_TYPE_KEY, MAP_SHOW_SATELLITE_VALUE)
    }


    private fun setMapNormal() {
        ibNormalMode?.isEnabled = false
        ibSatelliteMode?.isEnabled = true
        setIntInPreference(activity,MAP_SHOW_VIEW_TYPE_KEY, MAP_SHOW_NORMAL_VALUE)
    }

    //get the current size of sensors
    private fun getCurrentNumSensorsFromLocally(): Int? {
        val sensors= activity?.let { getSensorsFromLocally(it) }
        return sensors?.size
    }


    //add sensors according to the number that get from user
    private fun addSensors(){

        var numSensorsRequest:Int?=null

        val sensors= activity?.let { getSensorsFromLocally(it) }

        //check if id is already exist
        fun isIdExist(sensorsArr: ArrayList<Camera>, id: String): Boolean {
            for(item in sensorsArr){
                if(item.getId() == id){
                    return true
                }
            }
            return false
        }

        //ask before delete extra sensors
        fun askBeforeDeleteExtraSensor() {
            val dialog= AlertDialog.Builder(activity)
                //set message, title, and icon
                .setTitle(activity?.resources?.getString(com.sensoguard.hunter.R.string.remove_extra_sensors))
                .setMessage(
                    activity?.resources?.getString(
                        com.sensoguard.hunter.R.string.content_delete_extra_sensor
                    )
                ).setIcon(
                    android.R.drawable.ic_menu_delete

                )

                .setPositiveButton(activity?.resources?.getString(com.sensoguard.hunter.R.string.yes)) { dialog, _ ->

                    //remove extra sensors
                    if(numSensorsRequest!=null) {
                        val items=sensors?.listIterator()
                        while (items != null && items.hasNext()) {
                            val item = items.next()

                            val id=item.getId()
                            try {
                                if (id.toInt() > numSensorsRequest!!) {
                                    items.remove()
                                }
                            }catch(ex:NumberFormatException){
                                //do nothing
                            }
                        }
                        Toast.makeText(
                            activity,
                            resources.getString(com.sensoguard.hunter.R.string.sensors_save_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    sensors?.let { sen -> storeSensorsToLocally(sen, activity!!) }
                    dialog.dismiss()
                }


                .setNegativeButton(activity?.resources?.getString(com.sensoguard.hunter.R.string.no)) {
                        dialog, _ -> dialog.dismiss() }.create()
            dialog.show()

        }


        try{
            numSensorsRequest=etSensorValue?.text.toString().toInt()
        }catch (ex: NumberFormatException){
            Toast.makeText(this.context, "exception ${ex.message}", Toast.LENGTH_LONG).show()
            return
        }

        if(numSensorsRequest!=null
            && numSensorsRequest >254){
            Toast.makeText(
                this.context,
                resources.getString(com.sensoguard.hunter.R.string.invalid_mum_sensors),
                Toast.LENGTH_LONG
            ).show()
            return
        }


        if(numSensorsRequest!=null) {
            //add numSensors sensors
            for (sensorId in 1 until numSensorsRequest + 1) {
                //add it just if not exist
                if (sensors?.let { it1 -> !isIdExist(it1, sensorId.toString()) }!!) {
                    sensors.add(Camera(sensorId.toString()))
                }
            }
        }

        //check if the request of sensors number is smaller then the number of exist
        if(sensors?.size!=null
            && numSensorsRequest!=null
            && numSensorsRequest < sensors.size){
            askBeforeDeleteExtraSensor()
        }else if(activity!=null) {
            sensors?.let { sen -> storeSensorsToLocally(sen, activity!!) }
            Toast.makeText(
                activity,
                resources.getString(com.sensoguard.hunter.R.string.sensors_save_successfully),
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun showPopupList(anchorView: View) {
        if (LanguageManager.languagesItems != null && LanguageManager.languagesItems.size > 0) {
            generalItemMenuAdapter = GeneralItemMenuAdapter(
                activity,
                com.sensoguard.hunter.R.layout.item_general_menu,
                createLanguagesItemsDeliver(LanguageManager.languagesItems),
                this
            )
            listPopupWindow = ListPopupWindow(context)
            listPopupWindow?.isModal = true
            listPopupWindow?.animationStyle = com.sensoguard.hunter.R.style.winPopupAnimation
            listPopupWindow?.setAdapter(generalItemMenuAdapter)
            listPopupWindow?.anchorView = anchorView
            listPopupWindow?.width = getScreenWidth(activity) * 2 / 3
            listPopupWindow?.show()
        } else {
            Toast.makeText(
                activity,
                resources.getString(com.sensoguard.hunter.R.string.error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    //To prevent two identical list with the same address and depends on each other
    private fun createLanguagesItemsDeliver(items: java.util.ArrayList<GeneralItemMenu>): java.util.ArrayList<GeneralItemMenu> {
        val newItems = java.util.ArrayList<GeneralItemMenu>()
        newItems.addAll(items)
        return newItems
    }

    //select language for app
    override fun selectedItem(item: Any) {
        setStringInPreference(
            activity,
            CURRENT_LANG_KEY_PREF,
            GeneralItemMenu.selectedItem
        )
        if (item is GeneralItemMenu) {
            if (listPopupWindow != null && listPopupWindow!!.isShowing) {
                listPopupWindow?.dismiss()
                if (item != null) {
                    showCurrentLanguage(item)
                }
                listener?.updateLanguage()

            }
        }
    }
    //show current language in menu
    private fun showCurrentLanguage(generalItemMenu: GeneralItemMenu?) {
        if (generalItemMenu != null) {
            languageValue?.text = generalItemMenu.title
//            ibLangSelect.setImageDrawable(
//                ContextCompat.getDrawable(
//                    getMyActivity(),
//                    generalItemMenu.iconLarge
//                )
//            )
        }
    }

    //check if the fields is empty
    private fun validIsEmpty(editText: EditText): Boolean {
        var isValid = true

        if (editText.text.isNullOrBlank()) {
            editText.error =
                resources.getString(R.string.empty_field_error)
            isValid = false
        }

        return isValid
    }

    //start email validation
    private fun startServiceEmailValidation() {

        val serviceIntent = Intent(this.context, ServiceEmailValidation::class.java)

        //val intent = Intent()
        val cameraStr = myEmailAccount?.let { convertToGson(it) }
        cameraStr?.let {
            val bdl = Bundle()
            bdl.putString(CAMERA_KEY, cameraStr)
            serviceIntent.putExtras(bdl)
        }


        this.context?.let { ContextCompat.startForegroundService(it, serviceIntent) }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(receiver)
    }

    private fun setFilter() {
        val filter = IntentFilter(RESULT_VALIDATION_EMAIL_ACTION)
        filter.addAction(ERROR_RESULT_VALIDATION_EMAIL_ACTION)
        activity?.registerReceiver(receiver, filter)
    }


    //handle broadcast
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == RESULT_VALIDATION_EMAIL_ACTION) {
                pbValidationEmail?.visibility = View.GONE
                val resultValidationEmail = intent.getBooleanExtra(VALIDATION_EMAIL_RESULT, false)
                if (resultValidationEmail) {
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.validation_successfully),
                        Toast.LENGTH_LONG
                    ).show()
                    //store the email account locally
                    myEmailAccount?.let {
                        activity?.let { context ->
                            storeMyEmailAccountToLocaly(
                                it,
                                context
                            )
                        }
                    }
                }
            } else if (intent?.action == ERROR_RESULT_VALIDATION_EMAIL_ACTION) {
                val errorMsg = intent.getStringExtra(ERROR_VALIDATION_EMAIL_MSG_KEY)
                errorMsg?.let { Toast.makeText(activity, it, Toast.LENGTH_LONG).show() }
            }
        }

    }


}