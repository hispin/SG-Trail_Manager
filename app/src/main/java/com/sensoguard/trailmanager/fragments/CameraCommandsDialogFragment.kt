package com.sensoguard.trailmanager.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.global.CAMERA_KEY
import com.sensoguard.trailmanager.global.convertJsonToSensor

class CameraCommandsDialogFragment : DialogFragment(), View.OnClickListener {

    private var btnGetSnapshot: Button? = null
    private var btnDeleteAllImages: Button? = null
    private var btnGetBatteryStatus: Button? = null
    private var btnGetParameters: Button? = null
    private var btnArmCamera: Button? = null
    private var btnDisarmCamera: Button? = null
    private var btnSetEmailRecipients: Button? = null
    private var btnSetMmsRecipients: Button? = null
    private var btnSetAdmin: AppCompatButton? = null
    private var btnGetSnapshotMms: AppCompatButton? = null
    private var btnCancel: AppCompatButton? = null
    private var myCamera: Camera? = null
    private var tvCameraName: TextView? = null
    private var tvPhoneNum: TextView? = null


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


        btnCancel = view?.findViewById(R.id.btnCancel)
        btnCancel?.setOnClickListener {
            sendResult()
            //dismiss()
        }



        btnGetSnapshot = view?.findViewById(R.id.btnGetSnapshot)
        btnGetSnapshot?.setOnClickListener(this)
        btnDeleteAllImages = view?.findViewById(R.id.btnDeleteAllImages)
        btnDeleteAllImages?.setOnClickListener(this)
        btnGetBatteryStatus = view?.findViewById(R.id.btnGetBatteryStatus)
        btnGetBatteryStatus?.setOnClickListener(this)
        btnGetParameters = view?.findViewById(R.id.btnGetParameters)
        btnGetParameters?.setOnClickListener(this)
        btnArmCamera = view?.findViewById(R.id.btnArmCamera)
        btnArmCamera?.setOnClickListener(this)
        btnDisarmCamera = view?.findViewById(R.id.btnDisarmCamera)
        btnDisarmCamera?.setOnClickListener(this)
        btnSetEmailRecipients = view?.findViewById(R.id.btnSetEmailRecipients)
        btnSetEmailRecipients?.setOnClickListener(this)
        btnSetMmsRecipients = view?.findViewById(R.id.btnSetMmsRecipients)
        btnSetMmsRecipients?.setOnClickListener(this)
        btnSetAdmin = view?.findViewById(R.id.btnSetAdmin)
        btnSetAdmin?.setOnClickListener(this)
        btnGetSnapshotMms = view?.findViewById(R.id.btnGetSnapshotMms)
        btnGetSnapshotMms?.setOnClickListener(this)


        tvCameraName = view?.findViewById(R.id.tvCameraName)
        tvPhoneNum = view?.findViewById(R.id.tvPhoneNum)

    }

    override fun onClick(v: View?) {
        if (myCamera == null || myCamera?.phoneNum == null) {
            Toast.makeText(
                activity,
                resources.getString(R.string.missing_phone_number),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (myCamera?.cameraModel.equals("MG-983G-30M") ||
            myCamera?.cameraModel.equals("MG-984G-36M") ||
            myCamera?.cameraModel.equals("MG-984G-30M") ||
            myCamera?.cameraModel.equals("MG-983G-36M")
        ) {

            var command: String? = ""
            if (v?.id == R.id.btnGetSnapshot) {
                command = "#T#E#"
                sendSMS(command)
            } else if (v?.id == R.id.btnDeleteAllImages) {
                command = "#F#"
                sendSMS(command)
            } else if (v?.id == R.id.btnGetBatteryStatus) {
                command = "#C#"
                sendSMS(command)
            } else if (v?.id == R.id.btnGetParameters) {
                command = "#L#"
                sendSMS(command)
            } else if (v?.id == R.id.btnArmCamera) {
                command = "#A#"
                sendSMS(command)
            } else if (v?.id == R.id.btnDisarmCamera) {
                command = "#D#"
                sendSMS(command)
            } else if (v?.id == R.id.btnSetEmailRecipients) {
                showEmailsDialog()
            } else if (v?.id == R.id.btnSetMmsRecipients) {
                showPhoneNumDialog()
            } else if (v?.id == R.id.btnSetAdmin) {
                showSetAdminDialog()
            } else if (v?.id == R.id.btnGetSnapshotMms) {
                command = "#T#"
                sendSMS(command)
            }


        }
    }

    private fun sendSMS(command: String?) {
        val uri = Uri.parse("smsto:" + myCamera?.phoneNum)
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", command)
        startActivity(intent)
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
                command += etField1.text?.toString() + "#"
                command += etField2.text?.toString() + "#"
                command += etField3.text?.toString() + "#"
                command += etField4.text?.toString() + "#"
//                command = addEmailToCommand(command, etField1)
//                command = addEmailToCommand(command, etField2)
//                command = addEmailToCommand(command, etField3)
//                command = addEmailToCommand(command, etField4)
//                if (command == "#R#") {
//                    Toast.makeText(
//                        activity,
//                        resources.getString(R.string.you_need_at_least_one_email),
//                        Toast.LENGTH_LONG
//                    ).show()
//                } else {
                sendSMS(command)
                dialog.dismiss()
//                }

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
            myCommand += "$phnum#"
        }
//        if (etField.text != null
//            && etField.text.toString().isNotEmpty()
//            && !etField.text.toString().startsWith("000")
//        ) {
//            myCommand += etField.text?.toString() + "#"
//        }
        return myCommand
    }

    //show dialog with phone numbers fields
    private fun showPhoneNumDialog() {

        if (this@CameraCommandsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraCommandsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_phonenum_command)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)
            tvCommandTitle.text = resources.getString(R.string.add_phone_num_title)
            val etField1 = dialog.findViewById<AppCompatEditText>(R.id.etField1)
            val etField2 = dialog.findViewById<AppCompatEditText>(R.id.etField2)
            val etField3 = dialog.findViewById<AppCompatEditText>(R.id.etField3)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = "#N#"
                command = addPhoneNumToCommand(command, etField1)
                command = addPhoneNumToCommand(command, etField2)
                command = addPhoneNumToCommand(command, etField3)


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
                    val model = myCamera?.cameraModel?.replaceFirst("-", "")
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

    private fun sendResult() {
        if (targetFragment == null) {
            return
        }
        val intent = Intent()

        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, intent)
        dismiss()
    }


}