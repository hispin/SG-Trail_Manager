package com.sensoguard.trailmanager.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.adapters.CommandAdapter
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.classes.Command
import com.sensoguard.trailmanager.global.*
import com.sensoguard.trailmanager.interfaces.OnBackPressed
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class CameraCommandsDialogFragment : DialogFragment(), OnBackPressed {

    private var myCamera: Camera? = null
    private var tvCameraName: TextView? = null
    private var tvPhoneNum: TextView? = null
    private var mainCommands: ArrayList<Command>? = null
    private var moreCommands: ArrayList<Command>? = null
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
        if (myCamera?.cameraModel.equals(myModels[MG_MODEL]) ||
            myCamera?.cameraModel.equals(myModels[BG_MODEL])
        ) {
            mainCommands = ArrayList()
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
                    resources.getString(R.string.get_battery_status),
                    "#C#",
                    R.drawable.get_battery_status
                )
            )
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
            mainCommands?.add(
                Command(
                    resources.getString(R.string.set_admin_title),
                    null,
                    R.drawable.manager
                )
            )
            mainCommands?.add(
                Command(
                    resources.getString(R.string.delete_all_images),
                    "#F#",
                    R.drawable.delete_all_images
                )
            )
//            mainCommands?.add(
//                Command(
//                    resources.getString(R.string.more_orders),
//                    null,
//                    R.drawable.more
//                )
//            )
        } else if (myCamera?.cameraModel.equals(myModels[ATC_MODEL])) {
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


    private fun populateMoreCommandsByModel() {
        val myModels: Array<String> = resources.getStringArray(R.array.camera_model)
        if (myCamera?.cameraModel.equals(myModels[MG_MODEL])) {
            moreCommands = ArrayList()
        } else if (myCamera?.cameraModel.equals(myModels[ATC_MODEL])) {
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
        }

        commandsAdapter = activity?.let { adapter ->
            val myModels: Array<String> = resources.getStringArray(R.array.camera_model)
            commands?.let { arr ->
                CommandAdapter(arr, adapter) { command: Command ->
                    if (myCamera?.cameraModel.equals(myModels[MG_MODEL]) ||
                        myCamera?.cameraModel.equals(myModels[BG_MODEL])
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
                            resources.getString(R.string.more_orders) -> {
                                typeCommandList = MORE_COMMANDS_LIST_TYPE
                                refreshCommandsAdapter()
                            }
                            else -> {
                                sendSMS(command.commandContent)
                            }
                        }
                    } else if (myCamera?.cameraModel.equals(myModels[ATC_MODEL])) {
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


    //show dialog with emails fields
    private fun showEmailsDialog() {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_emails_commands)

            dialog.setCancelable(true)

            val etField1 = dialog.findViewById<AppCompatEditText>(R.id.etField1)
            val etField2 = dialog.findViewById<AppCompatEditText>(R.id.etField2)
            val etField3 = dialog.findViewById<AppCompatEditText>(R.id.etField3)
            val etField4 = dialog.findViewById<AppCompatEditText>(R.id.etField4)
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
            val etField3 = dialog.findViewById<AppCompatEditText>(R.id.etField3)
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
                    if (model.equals(myModels[MG_MODEL])) {
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


//    interface BackHandlerInterface {
//        fun setSelectedFragment(cameraCommandsDialogFragment: CameraCommandsDialogFragment?)
//    }
}