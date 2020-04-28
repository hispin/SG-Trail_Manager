package com.sensoguard.trailmanager.fragments

import android.app.Activity
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
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.classes.ImageStorageManager
import com.sensoguard.trailmanager.global.*
import java.util.regex.Pattern


class CameraExtraSettingsDialogFragment : DialogFragment() {

    //set as global in order to show it as big picture
    private var bitmap: Bitmap? = null
    private var btnSave: AppCompatButton? = null
    private var btnCancel: AppCompatButton? = null
    private var myCamera: Camera? = null
    private var ibTakePic: AppCompatImageButton? = null
    private var ibShowPicture: AppCompatImageButton? = null
    private var etSysName: AppCompatEditText? = null
    private var etTelNum: AppCompatEditText? = null

    private var spCameraType: AppCompatSpinner? = null
    private var pbValidationEmail: ProgressBar? = null
    private var ivModelImg: AppCompatImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_camera_extra_settings,
            container,
            false
        )
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
            if (isValidMobile(etTelNum?.text.toString())) {
                populateMyCamera()
                saveLastVisitPicture()
                //remove last date and enable scan last date
                removePreference(activity, LAST_DATE_ALARM)
                sendResult()
            } else {
                Toast.makeText(
                    activity,
                    resources.getString(R.string.invalid_telephone_number),
                    Toast.LENGTH_LONG
                ).show()
            }

        }

        btnCancel = view?.findViewById(R.id.btnCancel)
        btnCancel?.setOnClickListener {
            sendCancelResult()
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

        etSysName = view?.findViewById(R.id.etSysName)
        etTelNum = view?.findViewById(R.id.etTelNum)


        ivModelImg = view?.findViewById(R.id.ivModelImg)
        spCameraType = view?.findViewById(R.id.spCameraType)


        spCameraType?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val myModels: Array<String> = resources.getStringArray(R.array.camera_model)
                when (parent?.getItemAtPosition(position).toString()) {
                    myModels[MG_MODEL] -> {
                        ivModelImg?.setImageDrawable(this@CameraExtraSettingsDialogFragment.context?.let {
                            ContextCompat.getDrawable(
                                it, R.drawable.bg_668_img
                            )
                        })
                    }
                    myModels[BG_MODEL] -> {
                        ivModelImg?.setImageDrawable(this@CameraExtraSettingsDialogFragment.context?.let {
                            ContextCompat.getDrawable(
                                it, R.drawable.mg_983_img
                            )
                        })
                    }
                    myModels[ATC_MODEL] -> {
                        ivModelImg?.setImageDrawable(this@CameraExtraSettingsDialogFragment.context?.let {
                            ContextCompat.getDrawable(
                                it, R.drawable.atc_img
                            )
                        })
                    }
                }
                ivModelImg
            }

        }


        pbValidationEmail = view?.findViewById(R.id.pbValidationEmail)

    }


    private fun populateMyFields() {

        etSysName?.setText(myCamera?.getName())
        etTelNum?.setText(myCamera?.phoneNum)
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
        myCamera?.setName(etSysName?.text.toString())
        myCamera?.phoneNum = etTelNum?.text.toString()
        myCamera?.cameraModel = spCameraType?.selectedItem.toString()
        spCameraType?.selectedItemPosition?.let { myCamera?.cameraModelPosition = it }
    }

    //phone number validation
    private fun isValidMobile(phone: String): Boolean {
        return if (!Pattern.matches("[a-zA-Z]+", phone)) {
            phone.length in 10..16
        } else false
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

    private fun sendCancelResult() {
        if (targetFragment == null) {
            return
        }
        val intent = Intent()
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, intent)
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
            } else if (intent?.action == ERROR_RESULT_VALIDATION_EMAIL_ACTION) {
                val errorMsg = intent.getStringExtra(ERROR_VALIDATION_EMAIL_MSG_KEY)
                errorMsg?.let { Toast.makeText(activity, it, Toast.LENGTH_LONG).show() }
            }
        }

    }







}