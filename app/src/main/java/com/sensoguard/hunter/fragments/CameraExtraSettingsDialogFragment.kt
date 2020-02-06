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
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window.FEATURE_NO_TITLE
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


class CameraExtraSettingsDialogFragment : DialogFragment() {

    //set as global in order to show it as big picture
    private var bitmap: Bitmap? = null
    private var btnSave: AppCompatButton? = null
    private var btnCancel: AppCompatButton? = null
    private var myCamera: Camera? = null
    private var ibTakePic: AppCompatImageButton? = null
    private var ibShowPicture: AppCompatImageButton? = null
    private var etEmailAddress: AppCompatEditText? = null
    //    private var etEmailServer: AppCompatEditText? = null
//    private var etEmailPort: AppCompatEditText? = null
//    private var rgIsUseSSL: RadioGroup? = null
    private var etSysName: AppCompatEditText? = null
    private var etTelNum: MaskedEditText? = null
    // private var etPassword: AppCompatEditText? = null
    private var tvLastVisitValue: AppCompatTextView? = null
    private var ibOpenDatePicker: AppCompatImageButton? = null
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
//            Toast.makeText(
//                activity,
//                resources.getString(R.string.validation_successfully),
//                Toast.LENGTH_LONG
//            ).show()
            sendResult()
            //pbValidationEmail?.visibility = View.VISIBLE
            //startServiceEmailValidation()
            //sendResult()
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

        //etEmailServer = view?.findViewById(R.id.etMailServer)
        //etEmailPort = view?.findViewById(R.id.etMailServerPort)
        etEmailAddress = view?.findViewById(R.id.etMailAddress)
        //rgIsUseSSL = view?.findViewById(R.id.rgIsSSL)
        etSysName = view?.findViewById(R.id.etSysName)
        etTelNum = view?.findViewById(R.id.etTelNum)
        //etPassword = view?.findViewById(R.id.etPassword)
        tvLastVisitValue = view?.findViewById(R.id.tvLastVisitValue)
        ibOpenDatePicker = view?.findViewById(R.id.ibOpenDatePicker)




        ibOpenDatePicker?.setOnClickListener {
            if (context != null) {
                //open date picker to update the dates
                openDatePicker()
            }
        }


        spCameraType = view?.findViewById(R.id.spCameraType)
        pbValidationEmail = view?.findViewById(R.id.pbValidationEmail)

//        etEmailAddress?.setOnFocusChangeListener { view, hasFocus ->
//
//            if (!hasFocus) {
//                if ((view as EditText).text.toString().contains("@gmail.com")) {
//                    //fill automatically
//                    etEmailServer?.setText("imap.gmail.com")
//                    etEmailPort?.setText("993")
//                    rgIsUseSSL?.check(R.id.rbYes)
//                }
//            }
//        }
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
                    tvLastVisitValue?.text = selectedCalendar
                }, year, month, dayOfMonth
            )
        picker.show()
    }

    //populate camera object to return it main screen
    private fun populateMyFields() {
        //etEmailServer?.setText(myCamera?.emailServer)
        //etEmailPort?.setText(myCamera?.emailPort)
        etEmailAddress?.setText(myCamera?.emailAddress)

//if (myCamera?.isUseSSL != null && myCamera?.isUseSSL!!) {
//rgIsUseSSL?.check(R.id.rbYes)
//} else {
//rgIsUseSSL?.check(R.id.rbNo)
//}

        etSysName?.setText(myCamera?.getName())
        etTelNum?.setText(myCamera?.phoneNum)
        //etPassword?.setText(myCamera?.password)
        tvLastVisitValue?.text = myCamera?.lastVisitDate
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
        //myCamera?.emailServer = etEmailServer?.text.toString()
        //myCamera?.emailPort = etEmailPort?.text.toString()
        myCamera?.emailAddress = etEmailAddress?.text.toString()
        //myCamera?.isUseSSL = rgIsUseSSL?.checkedRadioButtonId == R.id.rbYes
        myCamera?.setName(etSysName?.text.toString())
        myCamera?.phoneNum = etTelNum?.text.toString()
        //myCamera?.password = etPassword?.text.toString()
        myCamera?.lastVisitDate = tvLastVisitValue?.text.toString()
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


}