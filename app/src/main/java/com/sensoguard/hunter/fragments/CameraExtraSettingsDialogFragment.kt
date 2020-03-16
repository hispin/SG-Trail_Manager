package com.sensoguard.hunter.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window.FEATURE_NO_TITLE
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import br.com.sapereaude.maskedEditText.MaskedEditText
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.classes.ImageStorageManager
import com.sensoguard.hunter.global.*
import com.sensoguard.hunter.services.ServiceEmailValidation1
import java.util.*


class CameraExtraSettingsDialogFragment : DialogFragment(), View.OnClickListener {

    //set as global in order to show it as big picture
    private var bitmap: Bitmap? = null
    private var btnSave: AppCompatButton? = null
    private var btnCancel: AppCompatButton? = null
    private var myCamera: Camera? = null
    private var ibTakePic: AppCompatImageButton? = null
    private var ibShowPicture: AppCompatImageButton? = null
    private var etEmailAddress: AppCompatEditText? = null
    private var etSysName: AppCompatEditText? = null
    private var etTelNum: MaskedEditText? = null
    private var btnGetSnapshot: Button? = null
    private var btnDeleteAllImages: Button? = null
    private var btnGetBatteryStatus: Button? = null
    private var btnGetParameters: Button? = null
    private var btnArmCamera: Button? = null
    private var btnDisarmCamera: Button? = null
    private var btnSetEmailRecipients: Button? = null
    private var btnSetMmsRecipients: Button? = null
    private var btnSetAdmin: AppCompatButton? = null
    //    private var tvLastVisitValue: AppCompatTextView? = null
//    private var ibOpenDatePicker: AppCompatImageButton? = null
    private var spCameraType: AppCompatSpinner? = null
    private var pbValidationEmail: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_camera_extra_settings,
            container,
            false
        )
        //prevent click below
        //dialog?.setCanceledOnTouchOutside(true)
        isCancelable = false


        initViews(view)


        val bundle = arguments
        val cameraStr = bundle?.getString(CAMERA_KEY, null)
        cameraStr?.let {
            myCamera = convertJsonToSensor(cameraStr)
            populateMyFields()
        }

        // Inflate the layout for this fragment
        return view
    }

    private fun initViews(view: View?) {
        btnSave = view?.findViewById(R.id.btnSave)
        btnSave?.setOnClickListener {
            populateMyCamera()
            saveLastVisitPicture()
            //remove last date and enable scan last date
            removePreference(activity, LAST_DATE_ALARM)
            sendResult()

        }

        btnCancel = view?.findViewById(R.id.btnCancel)
        btnCancel?.setOnClickListener {
            //(view.parent as ViewGroup).removeView(view)
            dismiss()
        }

        ibTakePic = view?.findViewById(R.id.ibTakePic)
        ibTakePic?.setOnClickListener {
            val myIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(myIntent, TAKE_PICTURE_REQUEST_CODE)
        }

        ibShowPicture = view?.findViewById(R.id.ibShowPicture)
        ibShowPicture?.setOnClickListener {
            openLargePictureDialog()
        }

        etEmailAddress = view?.findViewById(R.id.etMailAddress)
        etSysName = view?.findViewById(R.id.etSysName)
        etTelNum = view?.findViewById(R.id.etTelNum)


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
//        tvLastVisitValue = view?.findViewById(R.id.tvLastVisitValue)
//        ibOpenDatePicker = view?.findViewById(R.id.ibOpenDatePicker)


//        ibOpenDatePicker?.setOnClickListener {
//            if (context != null) {
//                //open date picker to update the dates
//                openDatePicker()
//            }
//        }


        spCameraType = view?.findViewById(R.id.spCameraType)
        pbValidationEmail = view?.findViewById(R.id.pbValidationEmail)

    }

    //open date picker to update the dates
    private fun openDatePicker() {
        val currentDate = Calendar.getInstance()
        val year = currentDate[Calendar.YEAR]
        val month = currentDate[Calendar.MONTH]
        val dayOfMonth = currentDate[Calendar.DAY_OF_MONTH]

        val picker =
            DatePickerDialog(
                context!!,
                OnDateSetListener { _, year1, month1, dayOfMonth1 ->
                    val selectedCalendar =
                        dayOfMonth1.toString() + "/" + (month1 + 1) + "/" + year1
                    //tvLastVisitValue?.text = selectedCalendar
                }, year, month, dayOfMonth
            )
        picker.show()
    }

    //populate camera object to return it main screen
    private fun populateMyFields() {
        //etEmailServer?.setText(myCamera?.emailServer)
        //etEmailPort?.setText(myCamera?.emailPort)
        etEmailAddress?.setText(myCamera?.emailAddress)


        etSysName?.setText(myCamera?.getName())
        etTelNum?.setText(myCamera?.phoneNum)
        //tvLastVisitValue?.text = myCamera?.lastVisitDate
        myCamera?.cameraModelPosition?.let { spCameraType?.setSelection(it) }
        myCamera?.lastVisitPicturePath?.let {
            if (context != null) {
                bitmap = ImageStorageManager.getImageFromInternalStorage(
                    context!!,
                    "image" + myCamera?.getId()
                )
                bitmap?.let { ibShowPicture?.setImageBitmap(bitmap) }
            }
        }
    }


    //populate camera object to return it main screen
    private fun populateMyCamera() {
        myCamera?.emailAddress = etEmailAddress?.text.toString()
        myCamera?.setName(etSysName?.text.toString())
        myCamera?.phoneNum = etTelNum?.text.toString()
        //myCamera?.lastVisitDate = tvLastVisitValue?.text.toString()
        myCamera?.cameraModel = spCameraType?.selectedItem.toString()
        spCameraType?.selectedItemPosition?.let { myCamera?.cameraModelPosition = it }
    }

    private fun saveLastVisitPicture() {
        bitmap?.let {
            if (context != null) {
                myCamera?.lastVisitPicturePath = ImageStorageManager.saveToInternalStorage(
                    context!!,
                    it,
                    "image" + myCamera?.getId()
                )
            }
        }
    }

    //open large picture
    private fun openLargePictureDialog() {
        if (context != null && bitmap != null) {
            val settingsDialog = Dialog(context!!)
            if (settingsDialog.window != null) {
                settingsDialog.window!!.requestFeature(FEATURE_NO_TITLE)
                val view = layoutInflater.inflate(R.layout.fragment_large_picture_video, null)
                settingsDialog.setContentView(view)
                val ibClose = view.findViewById<AppCompatImageButton>(R.id.ibClose)
                ibClose.setOnClickListener {
                    settingsDialog.dismiss()
                }

                val width = (bitmap?.width) as Int
                val height = (bitmap?.height) as Int

                val dialogWidth = getScreenWidth(activity) * 5 / 6
                val dialogHeight = (height / width) * dialogWidth///dialogWidth* 4 / 3

                //val newBitmap=Bitmap.createScaledBitmap(bitmap,dialogWidth,dialogHeight, true)
                //val newBitmap=Bitmap.createScaledBitmap(bitmap,(bitmap?.width?.times(10))as Int, (bitmap?.height?.times(10))as Int, true)

                val ivMyCaptureImage = view.findViewById<AppCompatImageView>(R.id.ivMyCaptureImage)
                //resize the image view according to the size of the bitmap
                ivMyCaptureImage.layoutParams.height = dialogHeight
                ivMyCaptureImage.layoutParams.width = dialogWidth
                ivMyCaptureImage.requestLayout()
                ivMyCaptureImage?.setImageBitmap(bitmap)//newBitmap
                settingsDialog.show()
            }
        }
    }

    private fun sendResult() {
        if (targetFragment == null) {
            return
        }
        val intent = Intent()
        val cameraStr = myCamera?.let { convertToGson(it) }
        cameraStr?.let {
            val bdl = Bundle()
            bdl.putString(CAMERA_KEY, cameraStr)
            intent.putExtras(bdl)
        }
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == TAKE_PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) run {
                bitmap = intent!!.extras!!.get("data") as Bitmap
                ibShowPicture?.setImageBitmap(bitmap)
            }
        }

    }

    //start email validation
    private fun startServiceEmailValidation() {

        val serviceIntent = Intent(this.context, ServiceEmailValidation1::class.java)

        //val intent = Intent()
        val cameraStr = myCamera?.let { convertToGson(it) }
        cameraStr?.let {
            val bdl = Bundle()
            bdl.putString(CAMERA_KEY, cameraStr)
            serviceIntent.putExtras(bdl)
        }


        this.context?.let { ContextCompat.startForegroundService(it, serviceIntent) }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(reciever)
    }

    private fun setFilter() {
        val filter = IntentFilter(RESULT_VALIDATION_EMAIL_ACTION)
        filter.addAction(ERROR_RESULT_VALIDATION_EMAIL_ACTION)
        activity?.registerReceiver(reciever, filter)
    }

    override fun onStart() {
        super.onStart()
        setFilter()
    }

    private val reciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == RESULT_VALIDATION_EMAIL_ACTION) {
                pbValidationEmail?.visibility = View.GONE
                val resultValidationEmail = intent.getBooleanExtra(VALIDATION_EMAIL_RESULT, false)
                if (resultValidationEmail) {
                    saveLastVisitPicture()
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.validation_successfully),
                        Toast.LENGTH_LONG
                    ).show()
                    sendResult()
                }
//                else{
//                    Toast.makeText(activity,resources.getString(R.string.validation_error),Toast.LENGTH_LONG).show()
//                }
            } else if (intent?.action == ERROR_RESULT_VALIDATION_EMAIL_ACTION) {
                val errorMsg = intent.getStringExtra(ERROR_VALIDATION_EMAIL_MSG_KEY)
                errorMsg?.let { Toast.makeText(activity, it, Toast.LENGTH_LONG).show() }
            }
        }

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

        if (this@CameraExtraSettingsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraExtraSettingsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_emails_commands)

            dialog.setCancelable(true)

            val etField1 = dialog.findViewById<AppCompatEditText>(R.id.etField1)
            val etField2 = dialog.findViewById<AppCompatEditText>(R.id.etField2)
            val etField3 = dialog.findViewById<AppCompatEditText>(R.id.etField3)
            val etField4 = dialog.findViewById<AppCompatEditText>(R.id.etField4)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = "#R#"
                command = addEmailToCommand(command, etField1)
                command = addEmailToCommand(command, etField2)
                command = addEmailToCommand(command, etField3)
                command = addEmailToCommand(command, etField4)
                if (command == "#R#") {
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.you_need_at_least_one_email),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    sendSMS(command)
                    dialog.dismiss()
                }

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


        if (etField.text != null
            && etField.text.toString().isNotEmpty()
            && !etField.text.toString().startsWith("000")
        ) {
            myCommand += etField.text?.toString() + "#"
        }
        return myCommand
    }

    //add phone number to command if it is not empty
    private fun addEmailToCommand(
        command: String,
        etField: AppCompatEditText
    ): String {

        var myCommand = command

        if (etField.text != null
            && etField.text.toString().isNotEmpty()
        ) {
            if (etField.text.toString().contains("@")) {
                myCommand += etField.text?.toString() + "#"
            } else {
                Toast.makeText(
                    activity,
                    resources.getString(R.string.invalid_email),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        return myCommand
    }

    //show dialog with phone numbers fields
    private fun showPhoneNumDialog() {

        if (this@CameraExtraSettingsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraExtraSettingsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_phonenum_command)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)
            tvCommandTitle.text = resources.getString(R.string.phone_num_title)
            val etField1 = dialog.findViewById<AppCompatEditText>(R.id.etField1)
            val etField2 = dialog.findViewById<AppCompatEditText>(R.id.etField2)
            val etField3 = dialog.findViewById<AppCompatEditText>(R.id.etField3)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = "#N#"
                command = addPhoneNumToCommand(command, etField1)
                command = addPhoneNumToCommand(command, etField2)
                command = addPhoneNumToCommand(command, etField3)
                if (command == "#N#") {
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.you_need_at_least_one_phone_num),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    sendSMS(command)
                    dialog.dismiss()
                }
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

        if (this@CameraExtraSettingsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraExtraSettingsDialogFragment.context!!)
            dialog.setContentView(R.layout.custom_dialog_set_admin_command)

            dialog.setCancelable(true)


            val etAdminPhone = dialog.findViewById<MaskedEditText>(R.id.etAdminPhone)
            val etPassword = dialog.findViewById<AppCompatEditText>(R.id.etPassword)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                if (!etAdminPhone.text.toString().startsWith("000")
                    && validIsEmpty(etPassword)
                ) {
                    var command = "#" + myCamera?.cameraModel + "#"
                    command += etPassword.text.toString() + "#"
                    command += etAdminPhone.text.toString() + "#"
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


}