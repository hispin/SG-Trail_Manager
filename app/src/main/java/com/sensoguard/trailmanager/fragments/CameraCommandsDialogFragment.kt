package com.sensoguard.trailmanager.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.NumberPicker.OnValueChangeListener
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.adapters.CommandAdapter
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.classes.Command
import com.sensoguard.trailmanager.classes.CommandConfigEmail
import com.sensoguard.trailmanager.global.*
import com.sensoguard.trailmanager.interfaces.OnBackPressed
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class CameraCommandsDialogFragment : DialogFragment(), OnBackPressed,
    CompoundButton.OnCheckedChangeListener {

    private var myCamera: Camera? = null
    private var tvCameraName: TextView? = null
    private var tvPhoneNum: TextView? = null
    private var mainCommands: ArrayList<Command>? = null
    private var moreCommands: ArrayList<Command>? = null
    private var newCameraCommands: ArrayList<Command>? = null
    private var rvCommands: RecyclerView? = null
    private var commandsAdapter: CommandAdapter? = null
    private var typeCommandList: Int = MAIN_COMMANDS_LIST_TYPE


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_camera_commands,
            container,
            false
        )


        initViews(view)

        val bundle = arguments
        val cameraStr = bundle?.getString(CAMERA_KEY, null)
        cameraStr?.let {
            myCamera = convertJsonToSensor(cameraStr)
            tvCameraName?.text = myCamera?.getName()
            tvPhoneNum?.setText(myCamera?.phoneNum)
        }


        // Inflate the layout for this fragment
        return view
    }

    private fun initViews(view: View?) {
        tvCameraName = view?.findViewById(R.id.tvCameraName)
        tvPhoneNum = view?.findViewById(R.id.tvPhoneNum)
        rvCommands = view?.findViewById(R.id.rvCommands)
    }

    override fun onStart() {
        super.onStart()
        refreshCommandsAdapter()
    }

    private fun populateCommandsByModel() {
        val myModels: Array<String> = resources.getStringArray(R.array.camera_model)
        if (myCamera?.cameraModel.equals(myModels[MODEL_984]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_668]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_310]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_584]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_636])
        ) {

            mainCommands = ArrayList()

            mainCommands?.add(
                Command(
                    resources.getString(R.string.setting_up_new_camera),
                    "#A#",
                    R.drawable.arm_camera
                )
            )

            //int model 584 hide those commands
            if (!myCamera?.cameraModel.equals(myModels[MODEL_584])) {
                mainCommands?.add(
                    Command(
                        resources.getString(R.string.arm_camera),
                        "#A#",
                        R.drawable.arm_camera
                    )
                )
                mainCommands?.add(
                    Command(
                        resources.getString(R.string.disarm_camera),
                        "#D#",
                        R.drawable.disarm_camera
                    )
                )
            }
            ///////////////////

            mainCommands?.add(
                Command(
                    resources.getString(R.string.time_synchronization),
                    "#E#T#",
                    R.drawable.parameters
                )
            )

            mainCommands?.add(
                Command(
                    resources.getString(R.string.get_snapshot_email),
                    "#T#E#",
                    R.drawable.get_snapshot_email
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.get_snapshot_mms),
                    "#T#",
                    R.drawable.get_snapshot_mms
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.get_parameters),
                    "#L#",
                    R.drawable.parameters
                )
            )

            mainCommands?.add(
                Command(
                    resources.getString(R.string.get_parameters_mms),
                    "#M#",
                    R.drawable.get_snapshot_mms
                )
            )


            mainCommands?.add(
                Command(
                    resources.getString(R.string.get_parameters_internet),
                    "#S#",
                    R.drawable.get_snapshot_email
                )
            )

            mainCommands?.add(
                Command(
                    resources.getString(R.string.get_battery_status),
                    "#C#",
                    R.drawable.get_battery_status
                )
            )


//            mainCommands?.add(
//                Command(
//                    resources.getString(R.string.help),
//                    "#H#",
//                    R.drawable.parameters
//                )
//            )

            mainCommands?.add(
                Command(
                    resources.getString(R.string.set_email_recipient),
                    null,
                    R.drawable.set_email_recipient
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.set_mms_recipients),
                    null,
                    R.drawable.set_mms_recipients
                )
            )
//            mainCommands?.add(
//                Command(
//                    resources.getString(R.string.set_admin_title),
//                    null,
//                    R.drawable.manager
//                )
//            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.delete_all_images),
                    "#F#",
                    R.drawable.delete_all_images
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.more_orders),
                    null,
                    R.drawable.more
                )
            )
        } else if (myCamera?.cameraModel.equals(myModels[MODEL_ATC])) {
            mainCommands = ArrayList()
            mainCommands?.add(
                Command(
                    resources.getString(R.string.arm_camera),
                    "*202#1#",
                    R.drawable.arm_camera
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.disarm_camera),
                    "*202#3#",
                    R.drawable.disarm_camera
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.take_a_proactive_picture),
                    "*500#",
                    R.drawable.get_snapshot_email
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.add_recipient_to_mms),
                    "*100#" + myCamera?.phoneNum + "#",
                    R.drawable.set_mms_recipients
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.add_recipient_to_email),
                    "*110#" + myCamera?.emailAddress + "#",
                    R.drawable.set_email_recipient
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.send_via_mms),
                    "*120#0#",
                    R.drawable.get_snapshot_mms
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.send_over_the_internet),
                    "*120#1#",
                    R.drawable.get_snapshot_email
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.receipt_only_in_mms),
                    "*130#0#",
                    R.drawable.get_snapshot_mms
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.reception_only_by_email),
                    "*130#1#",
                    R.drawable.get_snapshot_email
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.parameter_testing),
                    "*160#",
                    R.drawable.parameters
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.signal_testing),
                    "*150#",
                    R.drawable.parameters
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.battery_test),
                    "*201#",
                    R.drawable.get_battery_status
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.delete_all_images),
                    "*204#",
                    R.drawable.delete_all_images
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.more_orders),
                    null,
                    R.drawable.more
                )
            )
        }
    }

    //populate commands of new camera
    private fun populateCommandsOfNewCamera() {
        val myModels: Array<String> = resources.getStringArray(R.array.camera_model)
        if (myCamera?.cameraModel.equals(myModels[MODEL_984]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_668]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_310]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_584]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_636])
        ) {

            newCameraCommands = ArrayList()

            var command = Command(
                resources.getString(R.string.through_receiving_alerts),
                null,
                R.drawable.parameters
            )

            command.selectionsTitles.add("phone MMS")
            command.selectionsTitles.add("e-mail gprs")
            command.selectionsTitles.add("Molnus")
            command.selectionsCommands.add("#e#Mp#")
            command.selectionsCommands.add("#e#Mg#")
            command.selectionsCommands.add("#e#Mm#")

            newCameraCommands?.add(command)

            command = Command(
                resources.getString(R.string.configure_send),
                null,
                R.drawable.get_snapshot_mms
            )

            newCameraCommands?.add(command)


            command = Command(
                resources.getString(R.string.set_email_recipient),
                null,
                R.drawable.set_email_recipient
            )
            newCameraCommands?.add(command)

            command = Command(
                resources.getString(R.string.set_mms_recipients),
                null,
                R.drawable.set_mms_recipients
            )
            newCameraCommands?.add(command)
        }
    }

    private fun populateMoreCommandsByModel() {
        val myModels: Array<String> = resources.getStringArray(R.array.camera_model)
        if (myCamera?.cameraModel.equals(myModels[MODEL_984]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_668]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_310]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_584]) ||
            myCamera?.cameraModel.equals(myModels[MODEL_636])
        ) {

            moreCommands = ArrayList()
            var command = Command(
                resources.getString(R.string.setting_the_camera_mode),
                null,
                R.drawable.arm_camera
            )


            command.selectionsTitles.add(resources.getString(R.string.picture))
            command.selectionsTitles.add(resources.getString(R.string.video))
            command.selectionsTitles.add(resources.getString(R.string.picture_video))
            command.selectionsCommands.add("#e#cp#")
            command.selectionsCommands.add("#e#cv#")
            command.selectionsCommands.add("#e#ct#")
            moreCommands?.add(command)


            //picture quality
            command = Command(
                resources.getString(R.string.picture_quality),
                null,
                R.drawable.arm_camera
            )

            command.defaultSelected = 3
            if (myCamera?.cameraModel.equals(myModels[MODEL_668]) ||
                myCamera?.cameraModel.equals(myModels[MODEL_636]) ||
                myCamera?.cameraModel.equals(myModels[MODEL_984])
            ) {
                command.selectionsTitles.add("14MP")
                command.selectionsTitles.add("25MP")
                command.selectionsTitles.add("36MP")
                command.selectionsCommands.add("#e#s14#")
                command.selectionsCommands.add("#e#s25#")
                command.selectionsCommands.add("#e#s36#")
            } else if (myCamera?.cameraModel.equals(myModels[MODEL_310])
            ) {
                command.selectionsTitles.add("5MP")
                command.selectionsTitles.add("12MP")
                command.selectionsTitles.add("18MP")
                command.selectionsCommands.add("#e#s5#")
                command.selectionsCommands.add("#e#s12#")
                command.selectionsCommands.add("#e#s18#")

            } else if (myCamera?.cameraModel.equals(myModels[MODEL_584])
            ) {
                command.selectionsTitles.add("10MP")
                command.selectionsTitles.add("16MP")
                command.selectionsTitles.add("24MP")
                command.selectionsCommands.add("#e#s10#")
                command.selectionsCommands.add("#e#s16#")
                command.selectionsCommands.add("#e#s24#")

            }
            moreCommands?.add(command)

            //Continuous photo taking
            command = Command(
                resources.getString(R.string.continuous_photo_taking),
                null,
                R.drawable.arm_camera
            )

            if (myCamera?.cameraModel.equals(myModels[MODEL_668]) ||
                myCamera?.cameraModel.equals(myModels[MODEL_636]) ||
                myCamera?.cameraModel.equals(myModels[MODEL_584]) ||
                myCamera?.cameraModel.equals(myModels[MODEL_310]) ||
                myCamera?.cameraModel.equals(myModels[MODEL_984])
            ) {
                command.selectionsTitles.add(resources.getString(R.string.photos_1))
                command.selectionsTitles.add(resources.getString(R.string.photos_2))
                command.selectionsTitles.add(resources.getString(R.string.photos_3))
                command.selectionsCommands.add("#E#B1#")
                command.selectionsCommands.add("#E#B2#")
                command.selectionsCommands.add("#E#B3#")

                if (myCamera?.cameraModel.equals(myModels[MODEL_984])) {
                    command.selectionsTitles.add(resources.getString(R.string.photos_4))
                    command.selectionsTitles.add(resources.getString(R.string.photos_5))
                    command.selectionsCommands.add("#E#B4#")
                    command.selectionsCommands.add("#E#B5#")
                }
            }
            moreCommands?.add(command)


            //video quality
            command = Command(
                resources.getString(R.string.video_quality),
                null,
                R.drawable.arm_camera
            )
            if (myCamera?.cameraModel.equals(myModels[MODEL_668])) {
                command.selectionsTitles.add("720P")
                command.selectionsTitles.add("1080P")
                command.selectionsTitles.add("2k")
                command.selectionsTitles.add("4k")
                command.selectionsCommands.add("#E#FH#")
                command.selectionsCommands.add("#E#FF#")
                command.selectionsCommands.add("#E#F2#")
                command.selectionsCommands.add("#E#F4#")
                command.defaultSelected = 1
            } else if (
                myCamera?.cameraModel.equals(myModels[MODEL_636]) ||
                myCamera?.cameraModel.equals(myModels[MODEL_584]) ||
                myCamera?.cameraModel.equals(myModels[MODEL_984])
            ) {
                command.selectionsTitles.add("VGA")
                command.selectionsTitles.add("720P")
                command.selectionsTitles.add("1080P")
                command.selectionsCommands.add("#E#FL#")
                command.selectionsCommands.add("#E#FH#")
                command.selectionsCommands.add("#E#FF#")
                command.defaultSelected = 2
            } else if (myCamera?.cameraModel.equals(myModels[MODEL_310])) {
                command.selectionsTitles.add("VGA")
                command.selectionsTitles.add("720P")
                command.selectionsCommands.add("#E#FL#")
                command.selectionsCommands.add("#E#FH#")
                command.defaultSelected = 2
            }
            moreCommands?.add(command)

            //Video length
            command = Command(
                resources.getString(R.string.video_length),
                "#E#V",
                R.drawable.arm_camera
            )

            moreCommands?.add(command)

            //Motion sensor sensitivity
            command = Command(
                resources.getString(R.string.motion_sensor_sensitivity),
                null,
                R.drawable.parameters
            )
            command.defaultSelected = 3
            command.selectionsTitles.add(resources.getString(R.string.turned_off))
            command.selectionsTitles.add(resources.getString(R.string.high))
            command.selectionsTitles.add(resources.getString(R.string.normal))
            command.selectionsTitles.add(resources.getString(R.string.low))
            command.selectionsCommands.add("#e#po#")
            command.selectionsCommands.add("#e#ph#")
            command.selectionsCommands.add("#e#pn#")
            command.selectionsCommands.add("#e#pl#")

            moreCommands?.add(command)

            //File sending quality
            command = Command(
                resources.getString(R.string.file_sending_quality),
                null,
                R.drawable.get_snapshot_email
            )

            command.selectionsTitles.add(resources.getString(R.string.high))
            command.selectionsTitles.add(resources.getString(R.string.normal))
            command.selectionsTitles.add(resources.getString(R.string.low))
            command.selectionsCommands.add("#E#QH#")
            command.selectionsCommands.add("#E#QN#")
            command.selectionsCommands.add("#E#QL#")

            moreCommands?.add(command)

            //Frequency of sending alert
            command = Command(
                resources.getString(R.string.frequency_of_sending_alert),
                null,
                R.drawable.get_snapshot_email
            )
            command.defaultSelected = 2
            command.selectionsTitles.add(resources.getString(R.string.daily_report))
            command.selectionsTitles.add(resources.getString(R.string.real_time_alert))
            command.selectionsTitles.add(resources.getString(R.string.turned_off))
            command.selectionsCommands.add("#e#ed")
            command.selectionsCommands.add("#e#ei99#")
            command.selectionsCommands.add("#e#eo#")

            moreCommands?.add(command)

            //work hour
            command = Command(
                resources.getString(R.string.work_hour),
                null,
                R.drawable.parameters
            )
            command.defaultSelected = 2
            command.selectionsTitles.add(resources.getString(R.string.turned_on))
            command.selectionsTitles.add(resources.getString(R.string.turned_off))
            command.selectionsCommands.add("#e#HON")
            command.selectionsCommands.add("#e#HOFF#")

            moreCommands?.add(command)

            if (!myCamera?.cameraModel.equals(myModels[MODEL_584])) {
                //business days
                command = Command(
                    resources.getString(R.string.business_days),
                    null,
                    R.drawable.parameters
                )
                command.defaultSelected = 2
                command.selectionsTitles.add(resources.getString(R.string.turned_on))
                command.selectionsTitles.add(resources.getString(R.string.turned_off))
                command.selectionsCommands.add("#e#HON")
                command.selectionsCommands.add("#e#HOFF#")

                moreCommands?.add(command)
            }


            //remote control
            command = Command(
                resources.getString(R.string.remote_control),
                null,
                R.drawable.manager
            )
            command.defaultSelected = 1
            command.selectionsTitles.add(resources.getString(R.string.turned_on))
            command.selectionsTitles.add(resources.getString(R.string.turned_off))
            command.selectionsCommands.add("#e#ZON#")
            command.selectionsCommands.add("#e#ZOFF#")

            moreCommands?.add(command)

            //GPS
            if (myCamera?.cameraModel.equals(myModels[MODEL_636]) ||
                myCamera?.cameraModel.equals(myModels[MODEL_668])
            ) {
                command = Command(
                    resources.getString(R.string.gps),
                    null,
                    R.drawable.parameters
                )
                command.defaultSelected = 1
                command.selectionsTitles.add(resources.getString(R.string.turned_on))
                command.selectionsTitles.add(resources.getString(R.string.turned_off))
                command.selectionsCommands.add("#e#yon#")
                command.selectionsCommands.add("#e#yoff#")

                moreCommands?.add(command)
            }

            // Recycling bin
            command = Command(
                resources.getString(R.string.recycling_bin),
                null,
                R.drawable.delete_all_images
            )
            command.defaultSelected = 1
            command.selectionsTitles.add(resources.getString(R.string.turned_on))
            command.selectionsTitles.add(resources.getString(R.string.turned_off))
            command.selectionsCommands.add("#E#RON#")
            command.selectionsCommands.add("#E#ROFF#")

            moreCommands?.add(command)

            //Photography at regular intervals
            command = Command(
                resources.getString(R.string.photography_at_regular_intervals),
                null,
                R.drawable.parameters
            )
            command.defaultSelected = 1
            command.selectionsTitles.add(resources.getString(R.string.turned_off))
            command.selectionsTitles.add(resources.getString(R.string.minutes))
            command.selectionsTitles.add(resources.getString(R.string.hr))
            command.selectionsCommands.add("#E#T#L0#")
            command.selectionsCommands.add("#E#T#L")
            command.selectionsCommands.add("#E#T#L")

            moreCommands?.add(command)

            /////////////////////Model ATC
        } else if (myCamera?.cameraModel.equals(myModels[MODEL_ATC])) {
            moreCommands = ArrayList()
            moreCommands?.add(
                Command(
                    resources.getString(R.string.main_commands),
                    null,
                    R.drawable.more
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.delete_recipients_for_mms),
                    "*101#" + myCamera?.phoneNum + "#",
                    R.drawable.set_mms_recipients
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.delete_recipients_by_email),
                    "*111#" + myCamera?.emailAddress + "#",
                    R.drawable.set_email_recipient
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.receive_email_and_MMS),
                    "*130#2#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.sending_mode_operational),
                    "*140#0#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.sending_mode_daily_report),
                    "*140#1#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.sending_mode_off),
                    "*140#2#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.photo_mode_photo),
                    "*200#0#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.photo_mode_video),
                    "*200#1#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.photo_mode_photo_video),
                    "*200#2#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.image_quality_low),
                    "*190#0#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.image_quality_medium),
                    "*190#2#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.image_quality_high),
                    "*190#3#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.sensor_approaches_off),
                    "*202#3#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.sensor_sensitivity_high),
                    "*202#0#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.sensor_sensitivity_medium),
                    "*202#1#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.sensor_sensitivity_low),
                    "*202#2#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.enable_remote_control),
                    "*209#1#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.turn_off_remote_control),
                    "*209#0#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.update_time_and_date),
                    null,
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.selecting_quantity_of_pictures_unlimited),
                    "*180#999#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.image_quantity_selection_manual),
                    "*180#",
                    R.drawable.parameters
                )
            )
            moreCommands?.add(
                Command(
                    resources.getString(R.string.image_quantity_selection_zero_counter),
                    "*180#",
                    R.drawable.parameters
                )
            )
        }
    }


    private fun refreshCommandsAdapter() {

        var commands: ArrayList<Command>? = null
        if (typeCommandList == MAIN_COMMANDS_LIST_TYPE) {
            populateCommandsByModel()
            commands = mainCommands
        } else if (typeCommandList == MORE_COMMANDS_LIST_TYPE) {
            populateMoreCommandsByModel()
            commands = moreCommands
        } else if (typeCommandList == NEW_CAMERA_LIST_TYPE) {
            populateCommandsOfNewCamera()
            commands = newCameraCommands
        }

        commandsAdapter = activity?.let { adapter ->
            val myModels: Array<String> = resources.getStringArray(R.array.camera_model)
            commands?.let { arr ->
                CommandAdapter(arr, adapter) { command: Command ->
                    if (myCamera?.cameraModel.equals(myModels[MODEL_984]) ||
                        myCamera?.cameraModel.equals(myModels[MODEL_668]) ||
                        myCamera?.cameraModel.equals(myModels[MODEL_310]) ||
                        myCamera?.cameraModel.equals(myModels[MODEL_584]) ||
                        myCamera?.cameraModel.equals(myModels[MODEL_636])
                    ) {
                        when (command.commandName) {
                            resources.getString(R.string.set_email_recipient) -> {
                                showEmailsDialog()
                            }
                            resources.getString(R.string.set_mms_recipients) -> {
                                showPhoneNumbersDialog()
                            }
                            resources.getString(R.string.set_admin_title) -> {
                                showSetAdminDialog()
                            }
                            resources.getString(R.string.setting_the_camera_mode) -> {
                                showRadio3SelectedDialog(command)
                            }
                            resources.getString(R.string.picture_quality) -> {
                                showRadio3SelectedDialog(command)
                            }
                            resources.getString(R.string.continuous_photo_taking) -> {
                                if (command.selectionsCommands.size == 3) {
                                    showRadio3SelectedDialog(command)
                                } else if (command.selectionsCommands.size == 5) {
                                    showRadio5SelectedDialog(command)
                                }
                            }
                            resources.getString(R.string.video_quality) -> {
                                when (command.selectionsCommands.size) {
                                    3 -> {
                                        showRadio3SelectedDialog(command)
                                    }
                                    4 -> {
                                        showRadio4SelectedDialog(command)
                                    }
                                    2 -> {
                                        showRadio2SelectedDialog(command)
                                    }
                                }
                            }
                            resources.getString(R.string.video_length) -> {
                                if (myCamera?.cameraModel.equals(myModels[MODEL_584])) {
                                    showSpinnerSelectedDialog(command, 120)
                                } else {
                                    showSpinnerSelectedDialog(command, 180)
                                }
                            }
                            resources.getString(R.string.motion_sensor_sensitivity) -> {
                                showRadio4SelectedDialog(command)
                            }
                            resources.getString(R.string.file_sending_quality) -> {
                                showRadio3SelectedDialog(command)
                            }
                            resources.getString(R.string.frequency_of_sending_alert) -> {
                                showRadio3SelectedDialog(command)
                            }
                            resources.getString(R.string.work_hour) -> {
                                showRadio2SelectedDialog(command)
                            }
                            resources.getString(R.string.business_days) -> {
                                showSelectDaysDialog(command)
                            }
                            resources.getString(R.string.remote_control) -> {
                                showRadio2SelectedDialog(command)
                            }
                            resources.getString(R.string.gps) -> {
                                if (myCamera?.cameraModel.equals(myModels[MODEL_636]) ||
                                    myCamera?.cameraModel.equals(myModels[MODEL_668])
                                ) {
                                    showRadio2SelectedDialog(command)
                                }
                            }
                            resources.getString(R.string.recycling_bin) -> {
                                showRadio2SelectedDialog(command)
                            }
                            resources.getString(R.string.photography_at_regular_intervals) -> {
                                showRadio3WithSpinnerSelectedDialog(command)
                            }
                            resources.getString(R.string.setting_up_new_camera) -> {
                                typeCommandList = NEW_CAMERA_LIST_TYPE
                                refreshCommandsAdapter()
                            }
                            resources.getString(R.string.configure_send) -> {
                                showConfigurationSendingDialog(command)
                            }
                            resources.getString(R.string.more_orders) -> {
                                typeCommandList = MORE_COMMANDS_LIST_TYPE
                                refreshCommandsAdapter()
                            }
                            resources.getString(R.string.through_receiving_alerts) -> {
                                showRadio3SelectedDialog(command)
                            }
                            else -> {
                                sendSMS(command.commandContent)
                            }
                        }
                    } else if (myCamera?.cameraModel.equals(myModels[MODEL_ATC])) {
                        when (command.commandName) {
                            resources.getString(R.string.more_orders) -> {
                                typeCommandList = MORE_COMMANDS_LIST_TYPE
                                refreshCommandsAdapter()
                            }
                            resources.getString(R.string.update_time_and_date) -> {
                                val now: Calendar = Calendar.getInstance()
                                val dateStr = this@CameraCommandsDialogFragment.context?.let {
                                    getStringFromCalendar(
                                        now,
                                        "yyyyMMddHHmmss",
                                        it
                                    )
                                }
                                val cmd = "*205#$dateStr#"
                                sendSMS(cmd)
                            }
                            resources.getString(R.string.add_recipient_to_email) -> {
                                showEmailDialog(ADD_ACTION_TYPE)
                            }
                            resources.getString(R.string.delete_recipients_by_email) -> {
                                showEmailDialog(REMOVE_ACTION_TYPE)
                            }

                            resources.getString(R.string.add_recipient_to_mms) -> {
                                showPhoneNumberDialog(ADD_ACTION_TYPE)
                            }
                            resources.getString(R.string.delete_recipients_for_mms) -> {
                                showPhoneNumberDialog(REMOVE_ACTION_TYPE)
                            }

                            resources.getString(R.string.main_commands) -> {
                                typeCommandList = MAIN_COMMANDS_LIST_TYPE
                                refreshCommandsAdapter()
                            }
                            resources.getString(R.string.image_quantity_selection_manual) -> {
                                showSelectMultiImgManual()
                            }


                            else -> {
                                sendSMS(command.commandContent)
                            }
                        }
                    }
                }
            }

        }
        rvCommands?.adapter = commandsAdapter
        val layoutManager = LinearLayoutManager(activity)
        rvCommands?.layoutManager = layoutManager

        commandsAdapter?.notifyDataSetChanged()


    }

    //to disable when one of the checkboxes are unchecked
    var cbSelectAll: CheckBox? = null
    var isOneDisable = false

    //show dialog with days selection
    private fun showSelectDaysDialog(myCommand: Command) {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_select_days)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)
            tvCommandTitle.text = myCommand.commandName

            dialog.setCancelable(true)

            cbSelectAll = dialog.findViewById(R.id.cbSelectAll)


            val cbSunday = dialog.findViewById<CheckBox>(R.id.cbSunday)
            cbSunday.setOnCheckedChangeListener(this)
            val cbMonday = dialog.findViewById<CheckBox>(R.id.cbMonday)
            cbMonday.setOnCheckedChangeListener(this)
            val cbTuesday = dialog.findViewById<CheckBox>(R.id.cbTuesday)
            cbTuesday.setOnCheckedChangeListener(this)
            val cbWednesday = dialog.findViewById<CheckBox>(R.id.cbWednesday)
            cbWednesday.setOnCheckedChangeListener(this)
            val cbThursday = dialog.findViewById<CheckBox>(R.id.cbThursday)
            cbThursday.setOnCheckedChangeListener(this)
            val cbFriday = dialog.findViewById<CheckBox>(R.id.cbFriday)
            cbFriday.setOnCheckedChangeListener(this)
            val cbSaturday = dialog.findViewById<CheckBox>(R.id.cbSaturday)
            cbSaturday.setOnCheckedChangeListener(this)

            val cbDays = ArrayList<CheckBox>()
            cbDays.add(cbSunday)
            cbDays.add(cbMonday)
            cbDays.add(cbTuesday)
            cbDays.add(cbWednesday)
            cbDays.add(cbThursday)
            cbDays.add(cbFriday)
            cbDays.add(cbSaturday)

            //by default select all days
            toggleAllDays(cbDays, true)
            cbSelectAll?.isChecked = true

            //implements select all days
            cbSelectAll?.setOnCheckedChangeListener { cb, isChecked ->
                //check if the checkbox is pressed or the trigger is programmatically
                if (cb.isPressed) {
                    if (isChecked)
                        toggleAllDays(cbDays, true)
                    else
                        toggleAllDays(cbDays, false)
                }
            }


            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = "#e#D"
                command += cbSunday.isChecked.compareTo(false)
                command += cbMonday.isChecked.compareTo(false)
                command += cbTuesday.isChecked.compareTo(false)
                command += cbWednesday.isChecked.compareTo(false)
                command += cbThursday.isChecked.compareTo(false)
                command += cbFriday.isChecked.compareTo(false)
                command += cbSaturday.isChecked.compareTo(false)
                command += "#"

                sendSMS(command)
                dialog.dismiss()

            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    //select/un select all days
    private fun toggleAllDays(cbDays: ArrayList<CheckBox>, state: Boolean) {
        for (cb in cbDays)
            cb.isChecked = state
    }

    //show dialog with emails fields
    private fun showEmailsDialog() {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_emails_commands)

            dialog.setCancelable(true)

            val etField1 = dialog.findViewById<AppCompatEditText>(R.id.etField1)
            val etField2 = dialog.findViewById<AppCompatEditText>(R.id.etField2)
            val etField3 = dialog.findViewById<AppCompatEditText>(R.id.etMailServer)
            val etField4 = dialog.findViewById<AppCompatEditText>(R.id.etMailServerPort)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = "#R#"
                //insert "#" also if the there is no email
                //delete "-"
                var email = addEmailToCommand(etField1)
                if (email.equals("-1"))
                    return@setOnClickListener
                command += "$email#"
                email = addEmailToCommand(etField2)
                if (email.equals("-1"))
                    return@setOnClickListener
                command += "$email#"
                email = addEmailToCommand(etField3)
                if (email.equals("-1"))
                    return@setOnClickListener
                command += "$email#"
                email = addEmailToCommand(etField4)
                if (email.equals("-1"))
                    return@setOnClickListener
                command += "$email#"

                sendSMS(command)
                dialog.dismiss()

            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    //show dialog with email fields
    private fun showEmailDialog(actionType: Int) {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_add_recipient_to_email)

            dialog.setCancelable(true)

            val etField1 = dialog.findViewById<AppCompatEditText>(R.id.etField1)

            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = "*110#"
                if (actionType == REMOVE_ACTION_TYPE) {
                    command = "*111#"
                }
                //insert "#" also if the there is no email
                //delete "-"
                val email = addEmailToCommand(etField1)
                if (email.equals("-1"))
                    return@setOnClickListener
                command += "$email#"


                sendSMS(command)
                dialog.dismiss()

            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }


    }

    //show dialog with number selecting
    private fun showSelectMultiImgManual() {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_image_quantity_selection_manual)

            dialog.setCancelable(true)

            val numMultiSelectManual = dialog.findViewById<NumberPicker>(R.id.numMultiSelectManual)

            numMultiSelectManual.minValue = 1
            //Specify the maximum value/number of NumberPicker
            //Specify the maximum value/number of NumberPicker
            numMultiSelectManual.maxValue = 99

            //Gets whether the selector wheel wraps when reaching the min/max value.

            //Gets whether the selector wheel wraps when reaching the min/max value.
            numMultiSelectManual.wrapSelectorWheel = true

            //Set a value change listener for NumberPicker
            var myNum = 1

            //Set a value change listener for NumberPicker
            numMultiSelectManual.setOnValueChangedListener(OnValueChangeListener { picker, oldVal, newVal -> //Display the newly selected number from picker
                myNum = newVal
                //tv.setText("Selected Number : $newVal")
            })

            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = "*180#"

                //insert "#" also if the there is no email
                //delete "-"
//                val email = addEmailToCommand(etField1)
//                if (email.equals("-1"))
//                    return@setOnClickListener
                command += "$myNum#"


                sendSMS(command)
                dialog.dismiss()

            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }


    }


    //show dialog with spinner field
    private fun showSpinnerSelectedDialog(myCommand: Command, type: Int) {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_spinner_selections)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)
            tvCommandTitle.text = myCommand.commandName
            val spSelection = dialog.findViewById<AppCompatSpinner>(R.id.spSelection)
            //model 584 need only till 120
            if (type == 120) {
                val dataAdapter =
                    ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item
                    )
                val itemNames = resources.getStringArray(R.array.time_120_seconds)

                for (element in itemNames)  // Maximum size of i upto --> Your Array Size
                {
                    dataAdapter.add(element)
                }
                spSelection.adapter = dataAdapter
            }
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                val tmp = spSelection.selectedItem.toString().split(" ")
                if (tmp.size > 1) {
                    val command = myCommand.commandContent + tmp[0] + "#"

                    sendSMS(command)
                }
                dialog.dismiss()
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    //show dialog with 2 radio button fields
    private fun showRadio2SelectedDialog(myCommand: Command) {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_radio_2_selections)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)
            tvCommandTitle.text = myCommand.commandName
            val rbOne = dialog.findViewById<RadioButton>(R.id.rbOne)
            rbOne.text = myCommand.selectionsTitles[0]
            val rbTwo = dialog.findViewById<RadioButton>(R.id.rbTwo)
            rbTwo.text = myCommand.selectionsTitles[1]

            var btnFrom: Button? = null
            var btnUntil: Button? = null
            if (myCommand.commandName == resources.getString(R.string.work_hour)) {
                btnFrom = dialog.findViewById(R.id.btnFrom)
                btnUntil = dialog.findViewById(R.id.btnUntil)
                btnFrom.visibility = View.VISIBLE
                btnFrom.setOnClickListener {
                    val now = Calendar.getInstance()

                    openTimeDialog(
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        btnFrom
                    )
                }
                btnUntil.visibility = View.VISIBLE
                btnUntil.setOnClickListener {
                    val now = Calendar.getInstance()

                    openTimeDialog(
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        btnUntil
                    )
                }
            }

            when (myCommand.defaultSelected) {
                1 -> {
                    rbOne.isChecked = true
                }
                2 -> {
                    rbTwo.isChecked = true
                }
            }

            val rgSelects = dialog.findViewById<RadioGroup>(R.id.rgSelects)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = ""

                when (rgSelects.checkedRadioButtonId) {
                    R.id.rbOne -> {
                        // in work hour, add from-time and to-time
                        if (myCommand.commandName == resources.getString(R.string.work_hour)) {
                            if (!btnFrom?.text.toString()
                                    .contains(":") || !btnUntil?.text.toString().contains(":")
                            ) {
                                Toast.makeText(
                                    activity,
                                    resources.getString(R.string.select_an_hour),
                                    Toast.LENGTH_LONG
                                ).show()
                                return@setOnClickListener
                            } else {
                                command =
                                    myCommand.selectionsCommands[0] + btnFrom?.text.toString() + "-" + btnUntil?.text.toString() + "#"
                            }
                        } else {
                            command =
                                myCommand.selectionsCommands[0]
                        }
                    }
                    R.id.rbTwo -> {
                        command = myCommand.selectionsCommands[1]
                    }
                }

                sendSMS(command)
                dialog.dismiss()
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    //show dialog with 3 radio button fields
    private fun showRadio3SelectedDialog(myCommand: Command) {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_radio_3_selections)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)

            tvCommandTitle.text = myCommand.commandName

            val rbOne = dialog.findViewById<RadioButton>(R.id.rbOne)
            //unchecked all when daily report
//            if(myCommand.selectionsTitles[0] != resources.getString(R.string.daily_report)) {
//                rbOne.isChecked = true
//            }
            rbOne.text = myCommand.selectionsTitles[0]

            val rbTwo = dialog.findViewById<RadioButton>(R.id.rbTwo)
            rbTwo.text = myCommand.selectionsTitles[1]
            val rbThree = dialog.findViewById<RadioButton>(R.id.rbThree)
            rbThree.text = myCommand.selectionsTitles[2]
            val rgSelects = dialog.findViewById<RadioGroup>(R.id.rgSelects)

            when (myCommand.defaultSelected) {
                1 -> {
                    rbOne.isChecked = true
                }
                2 -> {
                    rbTwo.isChecked = true
                }
                3 -> {
                    rbThree.isChecked = true
                }
            }

            rbOne.setOnCheckedChangeListener { _, isChecked ->
                if (myCommand.selectionsTitles[0] == resources.getString(R.string.daily_report)) {
                    if (isChecked) {
                        val now = Calendar.getInstance()

                        openTimeDialog(
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            rbOne
                        )
                    }
                }
            }

            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = ""

                when (rgSelects.checkedRadioButtonId) {
                    R.id.rbOne -> {
                        //in daily report add the time
                        if (myCommand.selectionsTitles[0] == resources.getString(R.string.daily_report)) {
                            val tmp =
                                myCommand.selectionsCommands[0] + selectedHour + ":" + selectedMinute + "#"
                            command = tmp
                        } else {
                            command = myCommand.selectionsCommands[0]
                        }

                    }
                    R.id.rbTwo -> {
                        command = myCommand.selectionsCommands[1]
                    }
                    R.id.rbThree -> {
                        command = myCommand.selectionsCommands[2]
                    }
                }

                sendSMS(command)
                dialog.dismiss()
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    //show dialog with 3 radio button fields with spinner
    private fun showRadio3WithSpinnerSelectedDialog(myCommand: Command) {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_radio_3_with_spinner_selections)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)

            tvCommandTitle.text = myCommand.commandName

            val rbOne = dialog.findViewById<RadioButton>(R.id.rbOne)
            //unchecked all when daily report
//            if(myCommand.selectionsTitles[0] != resources.getString(R.string.daily_report)) {
//                rbOne.isChecked = true
//            }
            rbOne.text = myCommand.selectionsTitles[0]

            val rbTwo = dialog.findViewById<RadioButton>(R.id.rbTwo)
            rbTwo.text = myCommand.selectionsTitles[1]
            val rbThree = dialog.findViewById<RadioButton>(R.id.rbThree)
            rbThree.text = myCommand.selectionsTitles[2]
            val rgSelects = dialog.findViewById<RadioGroup>(R.id.rgSelects)

            val spSeconds = dialog.findViewById<AppCompatSpinner>(R.id.spSeconds)
            spSeconds.isEnabled = false
            val spHours = dialog.findViewById<AppCompatSpinner>(R.id.spHours)
            spHours.isEnabled = false

            when (myCommand.defaultSelected) {
                1 -> {
                    rbOne.isChecked = true
                }
                2 -> {
                    rbTwo.isChecked = true
                }
                3 -> {
                    rbThree.isChecked = true
                }
            }

            rbTwo.setOnCheckedChangeListener { _, isChecked ->

                spSeconds.isEnabled = isChecked

            }
            rbThree.setOnCheckedChangeListener { _, isChecked ->

                spHours.isEnabled = isChecked

            }


            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = ""

                when (rgSelects?.checkedRadioButtonId) {
                    R.id.rbOne -> {
                        command = myCommand.selectionsCommands[0]

                    }
                    R.id.rbTwo -> {
                        command = myCommand.selectionsCommands[1]
                        val tmp = spSeconds?.selectedItem.toString().split(" ")
                        if (tmp.size > 1) {
                            command += tmp[0] + "m#"
                        }
                    }
                    R.id.rbThree -> {
                        command = myCommand.selectionsCommands[2]
                        val tmp = spHours?.selectedItem.toString().split(" ")
                        if (tmp.size > 1) {
                            command += tmp[0] + "h#"
                        }
                    }
                }

                sendSMS(command)
                dialog.dismiss()
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }


    //show dialog with 4 radio button fields
    private fun showRadio4SelectedDialog(myCommand: Command) {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_radio_4_selections)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)
            tvCommandTitle.text = myCommand.commandName
            val rbOne = dialog.findViewById<RadioButton>(R.id.rbOne)
            rbOne.text = myCommand.selectionsTitles[0]
            val rbTwo = dialog.findViewById<RadioButton>(R.id.rbTwo)
            rbTwo.text = myCommand.selectionsTitles[1]
            val rbThree = dialog.findViewById<RadioButton>(R.id.rbThree)
            rbThree.text = myCommand.selectionsTitles[2]
            val rbFour = dialog.findViewById<RadioButton>(R.id.rbFour)
            rbFour.text = myCommand.selectionsTitles[3]

            when (myCommand.defaultSelected) {
                1 -> {
                    rbOne.isChecked = true
                }
                2 -> {
                    rbTwo.isChecked = true
                }
                3 -> {
                    rbThree.isChecked = true
                }
                4 -> {
                    rbFour.isChecked = true
                }
            }

//            if(myCommand.commandName == resources.getString(R.string.motion_sensor_sensitivity)){
//                rbThree.isChecked = true
//            }else {
//                rbOne.isChecked = true
//            }
            val rgSelects = dialog.findViewById<RadioGroup>(R.id.rgSelects)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = ""

                when (rgSelects.checkedRadioButtonId) {
                    R.id.rbOne -> {
                        command = myCommand.selectionsCommands[0]
                    }
                    R.id.rbTwo -> {
                        command = myCommand.selectionsCommands[1]
                    }
                    R.id.rbThree -> {
                        command = myCommand.selectionsCommands[2]
                    }
                    R.id.rbFour -> {
                        command = myCommand.selectionsCommands[3]
                    }
                }

                sendSMS(command)
                dialog.dismiss()
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    //show dialog with configuration sending
    private fun showConfigurationSendingDialog(myCommand: Command) {
        var commandConfigEmail: CommandConfigEmail? = null

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_configuration_send)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)
            tvCommandTitle.text = myCommand.commandName

            addCommandConfigMMS(myCommand)

            val rgConfigSelects = dialog.findViewById<RadioGroup>(R.id.rgConfigSelects)
            val rbHotWe4g = dialog.findViewById<RadioButton>(R.id.rbHotWe4g)
            rgConfigSelects.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.rbConfigMms -> {
                        rbHotWe4g.isEnabled = false
                        addCommandConfigMMS(myCommand)
                    }
                    R.id.rbConfigEmail -> {
                        myCommand.selectionsCommands = ArrayList()
                        rbHotWe4g.isEnabled = true
                        commandConfigEmail = CommandConfigEmail()
                        commandConfigEmail?.showPhoneNumbersDialog(this@CameraCommandsDialogFragment.context)
                    }

                }

            }

            val rgSelects = dialog.findViewById<RadioGroup>(R.id.rgSelects)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = ""

                if (rgConfigSelects.checkedRadioButtonId == R.id.rbConfigEmail) {
                    if (commandConfigEmail != null) {
                        myCommand.selectionsCommands = commandConfigEmail!!.populateCommands()
                    }
                }

                if (myCommand.selectionsCommands.size < 1) {
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.no_commands),
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                when (rgSelects.checkedRadioButtonId) {
                    R.id.rbCellcom -> {
                        command = myCommand.selectionsCommands[0]
                    }
                    R.id.rbGolanTelecom -> {
                        command = myCommand.selectionsCommands[1]
                    }
                    R.id.rbPartner -> {
                        command = myCommand.selectionsCommands[2]
                    }
                    R.id.rbCellPhone -> {
                        command = myCommand.selectionsCommands[3]
                    }
                    R.id.rbHotMobile -> {
                        command = myCommand.selectionsCommands[4]
                    }
                    R.id.rb019 -> {
                        command = myCommand.selectionsCommands[5]
                    }
                    R.id.rbRamiLevi -> {
                        command = myCommand.selectionsCommands[6]
                    }
                    R.id.rbYouPhone -> {
                        command = myCommand.selectionsCommands[7]
                    }
                    R.id.rb012 -> {
                        command = myCommand.selectionsCommands[8]
                    }
                    R.id.rbHotWe4g -> {
                        command = myCommand.selectionsCommands[9]
                    }
                    else -> {
                        Toast.makeText(
                            activity,
                            resources.getString(R.string.no_commands),
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }

                }

                sendSMS(command)
                dialog.dismiss()
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }


    //show dialog with 5 radio button fields
    private fun showRadio5SelectedDialog(myCommand: Command) {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_radio_5_selections)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)
            tvCommandTitle.text = myCommand.commandName
            val rbOne = dialog.findViewById<RadioButton>(R.id.rbOne)
            rbOne.text = myCommand.selectionsTitles[0]
            val rbTwo = dialog.findViewById<RadioButton>(R.id.rbTwo)
            rbTwo.text = myCommand.selectionsTitles[1]
            val rbThree = dialog.findViewById<RadioButton>(R.id.rbThree)
            rbThree.text = myCommand.selectionsTitles[2]
            val rbFour = dialog.findViewById<RadioButton>(R.id.rbFour)
            rbFour.text = myCommand.selectionsTitles[3]
            val rbFive = dialog.findViewById<RadioButton>(R.id.rbFive)
            rbFive.text = myCommand.selectionsTitles[4]

            when (myCommand.defaultSelected) {
                1 -> {
                    rbOne.isChecked = true
                }
                2 -> {
                    rbTwo.isChecked = true
                }
                3 -> {
                    rbThree.isChecked = true
                }
                4 -> {
                    rbFour.isChecked = true
                }
                5 -> {
                    rbFive.isChecked = true
                }
            }

            val rgSelects = dialog.findViewById<RadioGroup>(R.id.rgSelects)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = ""

                when (rgSelects.checkedRadioButtonId) {
                    R.id.rbOne -> {
                        command = myCommand.selectionsCommands[0]
                    }
                    R.id.rbTwo -> {
                        command = myCommand.selectionsCommands[1]
                    }
                    R.id.rbThree -> {
                        command = myCommand.selectionsCommands[2]
                    }
                    R.id.rbFour -> {
                        command = myCommand.selectionsCommands[4]
                    }
                    R.id.rbFive -> {
                        command = myCommand.selectionsCommands[5]
                    }
                }

                sendSMS(command)
                dialog.dismiss()
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    //show dialog with phone numbers fields
    private fun showPhoneNumbersDialog() {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_phonenum_command)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)
            tvCommandTitle.text = resources.getString(R.string.add_phone_numbers_title)
            val etField1 = dialog.findViewById<AppCompatEditText>(R.id.etField1)
            val etField2 = dialog.findViewById<AppCompatEditText>(R.id.etField2)
            val etField3 = dialog.findViewById<AppCompatEditText>(R.id.etMailServer)
            val etField4 = dialog.findViewById<AppCompatEditText>(R.id.etMailServerPort)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = "#N#"
                command = addPhoneNumToCommand(command, etField1)
                if (command == "-1") {
                    return@setOnClickListener
                }
                command = addPhoneNumToCommand(command, etField2)
                if (command == "-1") {
                    return@setOnClickListener
                }
                command = addPhoneNumToCommand(command, etField3)
                if (command == "-1") {
                    return@setOnClickListener
                }
                command = addPhoneNumToCommand(command, etField4)
                if (command == "-1") {
                    return@setOnClickListener
                }


                sendSMS(command)
                dialog.dismiss()
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    //show dialog with phone number fields
    private fun showPhoneNumberDialog(actionType: Int) {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_add_recipient_to_mms)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)
            tvCommandTitle.text = resources.getString(R.string.add_phone_numbers_title)
            val etField1 = dialog.findViewById<AppCompatEditText>(R.id.etField1)

            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = "*100#"
                if (actionType == REMOVE_ACTION_TYPE) {
                    command = "*101#"
                }
                command = addPhoneNumToCommand(command, etField1)
                if (command == "-1") {
                    return@setOnClickListener
                }

                sendSMS(command)
                dialog.dismiss()
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    //add phone number to command if it is not empty
    private fun addPhoneNumToCommand(
        command: String,
        etField: AppCompatEditText
    ): String {

        var myCommand = command
        if (etField.text.toString().startsWith("000")) {
            myCommand += "#"
        } else {
            //delete "-"
            var phnum = etField.text?.toString()
            phnum = phnum?.replace("-", "")

            if (phnum != null && isValidMobile(phnum)) {
                myCommand += "$phnum#"
            } else if (phnum.equals("")) {
                myCommand += "#"
            } else {
                etField.error = resources.getString(R.string.invalid_telephone_number)
                return "-1"
            }


        }
        return myCommand
    }

    //validate email,if it is not empty the it must contain @
    private fun addEmailToCommand(etField: AppCompatEditText): String? {
        val email = etField.text?.toString()
        if (email.equals("")) return email
        else if (email != null && email.contains("@")) return email
        else {
            etField.error = resources.getString(R.string.email_must_contain)
            return "-1"
        }
    }

    //phone number validation
    private fun isValidMobile(phone: String): Boolean {
        return if (!Pattern.matches("[a-zA-Z]+", phone)) {
            phone.length in 9..16
        } else false
    }

    //show dialog to set admin
    private fun showSetAdminDialog() {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_set_admin_command)

            dialog.setCancelable(true)


            val etAdminPhone = dialog.findViewById<AppCompatEditText>(R.id.etAdminPhone)
            val etPassword = dialog.findViewById<AppCompatEditText>(R.id.etPassword)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                if (!etAdminPhone.text.toString().startsWith("000")
                    && validIsEmpty(etPassword)
                ) {
                    var model = myCamera?.cameraModel?.replaceFirst("-", "")
                    val myModels: Array<String> = resources.getStringArray(R.array.camera_model)
                    if (model.equals(myModels[MODEL_984])) {
                        model = "MG984-36M"
                    }
                    var command = "#$model#"
                    command += etPassword.text.toString() + "#"
                    var phAdmin = etAdminPhone.text.toString()
                    phAdmin = phAdmin.replace("-", "")
                    command += "$phAdmin#"
                    sendSMS(command)
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.incorrect_phone_number_or_password),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun validIsEmpty(editText: EditText): Boolean {
        var isValid = true

        if (editText.text.isNullOrBlank()) {
            editText.error =
                resources.getString(R.string.empty_field_error)
            isValid = false
        }

        return isValid
    }

    private fun sendSMS(command: String?) {
        val uri = Uri.parse("smsto:" + myCamera?.phoneNum)
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", command)
        startActivity(intent)
    }

    override fun onBackPressed(): Boolean {
        if (typeCommandList == MAIN_COMMANDS_LIST_TYPE) {
            return true
        }
        typeCommandList = MAIN_COMMANDS_LIST_TYPE
        refreshCommandsAdapter()
        return false
    }

    var tp: TimePickerDialog? = null
    var selectedHour: Int? = null
    var selectedMinute: Int? = null

    //time dialog with radio buttons
    private fun openTimeDialog(mHour: Int, mMinute: Int, rbOne: RadioButton) {
        tp = TimePickerDialog(
            context,
            R.style.DateTimeDialog,
            { _, hour, minute ->

                tp?.cancel()

                selectedHour = hour
                selectedMinute = minute
                val tmp =
                    resources.getString(R.string.daily_report) + " " + selectedHour + ":" + selectedMinute
                rbOne.text = tmp


            },
            mHour,
            mMinute,
            true
        )

        tp?.setOnDismissListener {
            rbOne.isChecked = false
        }

        tp?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        tp?.show()
    }

    //time dialog with buttons
    private fun openTimeDialog(mHour: Int, mMinute: Int, btn: Button) {
        tp = TimePickerDialog(
            context,
            R.style.DateTimeDialog,
            { _, hour, minute ->

                tp?.cancel()

                selectedHour = hour
                selectedMinute = minute
                val tmp = selectedHour.toString() + ":" + selectedMinute.toString()
                btn.text = tmp


            },
            mHour,
            mMinute,
            true
        )

        tp?.setOnDismissListener {
            //btn.isChecked=false
        }

        tp?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        tp?.show()
    }


    //        //add command when selecting configuration MMS
    fun addCommandConfigMMS(command: Command) {
//            command.selectionsTitles.add(resources.getString(R.string.cellcom))
//            command.selectionsTitles.add(resources.getString(R.string.golan_telecom))
//            command.selectionsTitles.add(resources.getString(R.string.partner))
//            command.selectionsTitles.add(resources.getString(R.string.cell_phone))
//            command.selectionsTitles.add(resources.getString(R.string.hot_mobile))
//            command.selectionsTitles.add("019")
//            command.selectionsTitles.add(resources.getString(R.string.rami_levi))
//            command.selectionsTitles.add("YouPhone")
//            command.selectionsTitles.add("012")


        command.selectionsCommands.add(0, "#M#http://mms.cellcom.co.il#172.31.29.38#8080#mms###")
        command.selectionsCommands.add(
            1,
            "#M#http://10.224.228.81#10.224.228.81#80#mms.golantelecom.net.il###"
        )
        command.selectionsCommands.add(
            2,
            "#M#http://192.168.220.15/servlets/mms#192.118.11.55#8080#uwap.orange.co.il###"
        )
        command.selectionsCommands.add(
            3,
            "#M#http://mmsu.pelephone.net.il#10.170.9.54#9093#mms.pelephone.net.il####"
        )
        command.selectionsCommands.add(
            4,
            "#M#http://mms.hotmobile.co.il#80.246.131.99#80#mms.hotm###"
        )
        command.selectionsCommands.add(5, "#M####mms###")
        command.selectionsCommands.add(6, "#M##10.170.9.54#9093#MMS.rl#rl@3g#rl#")
        command.selectionsCommands.add(
            7,
            "#M#http://192.168.220.15/servlets/mms##80#data.youphone.co.il###"
        )
        command.selectionsCommands.add(
            8,
            "#M#http://192.168.220.15/servlets/mms#172.31.29.38#8080#MMS###"
        )
    }

    //handler just for business days
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (!isChecked)
            cbSelectAll?.isChecked = false
    }


}