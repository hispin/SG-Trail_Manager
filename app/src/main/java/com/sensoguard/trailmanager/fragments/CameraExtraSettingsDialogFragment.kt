package com.sensoguard.trailmanager.fragments

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window.FEATURE_NO_TITLE
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.classes.ImageStorageManager
import com.sensoguard.trailmanager.global.CAMERA_EXTRA_SETTINGS_RESULT_KEY
import com.sensoguard.trailmanager.global.CAMERA_KEY
import com.sensoguard.trailmanager.global.ERROR_RESULT_VALIDATION_EMAIL_ACTION
import com.sensoguard.trailmanager.global.ERROR_VALIDATION_EMAIL_MSG_KEY
import com.sensoguard.trailmanager.global.LAST_DATE_ALARM
import com.sensoguard.trailmanager.global.MODEL_310
import com.sensoguard.trailmanager.global.MODEL_410
import com.sensoguard.trailmanager.global.MODEL_584
import com.sensoguard.trailmanager.global.MODEL_636
import com.sensoguard.trailmanager.global.MODEL_636_48MP
import com.sensoguard.trailmanager.global.MODEL_668
import com.sensoguard.trailmanager.global.MODEL_984
import com.sensoguard.trailmanager.global.MODEL_ATC
import com.sensoguard.trailmanager.global.RESULT_IS_OK_KEY
import com.sensoguard.trailmanager.global.RESULT_VALIDATION_EMAIL_ACTION
import com.sensoguard.trailmanager.global.VALIDATION_EMAIL_RESULT
import com.sensoguard.trailmanager.global.convertJsonToSensor
import com.sensoguard.trailmanager.global.convertToGson
import com.sensoguard.trailmanager.global.getScreenWidth
import com.sensoguard.trailmanager.global.removePreference
import java.util.regex.Pattern


//settings of a camera (name, phone, model, picture of the last visit)
//the result is returned to CamerasFragment with the key CAMERA_EXTRA_SETTINGS_RESULT_KEY
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
    private var tvShowVer: AppCompatTextView? = null

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

        val cameraStr = arguments?.getString(CAMERA_KEY, null)
        cameraStr?.let {
            myCamera = convertJsonToSensor(cameraStr)
            populateMyFields()
        }

        // Inflate the layout for this fragment
        return view
    }

    //the image of each camera model (by the position of the model in R.array.camera_model)
    private fun getModelImage(modelPosition: Int): Int? {
        return when (modelPosition) {
            MODEL_984 -> R.drawable.mg_983_img
            MODEL_668 -> R.drawable.bg_668_img
            MODEL_ATC -> R.drawable.atc_img
            MODEL_310, MODEL_410 -> R.drawable.ic_camera310
            MODEL_584 -> R.drawable.ic_camera584
            MODEL_636, MODEL_636_48MP -> R.drawable.ic_camera636
            else -> null
        }
    }

    private fun initViews(view: View) {
        btnSave = view.findViewById(R.id.btnSave)
        btnSave?.setOnClickListener {
            if (isValidMobile(etTelNum?.text.toString())) {
                populateMyCamera()
                saveLastVisitPicture()
                //remove last date and enable scan last date
                removePreference(activity, LAST_DATE_ALARM)
                sendResult()
            } else {
                etTelNum?.error = resources.getString(R.string.invalid_telephone_number)
            }

        }

        btnCancel = view.findViewById(R.id.btnCancel)
        btnCancel?.setOnClickListener {
            sendCancelResult()
        }

        ibTakePic = view.findViewById(R.id.ibTakePic)
        ibTakePic?.setOnClickListener {
            takePictureLauncher.launch(null)
        }

        ibShowPicture = view.findViewById(R.id.ibShowPicture)
        ibShowPicture?.setOnClickListener {
            openLargePictureDialog()
        }

        etSysName = view.findViewById(R.id.etSysName)
        etTelNum = view.findViewById(R.id.etTelNum)

        tvShowVer = view.findViewById(R.id.tvShowVer)

        val verName =
            activity?.let { it.packageManager.getPackageInfo(it.packageName, 0).versionName }
        tvShowVer?.text = getString(R.string.version_title, verName)

        ivModelImg = view.findViewById(R.id.ivModelImg)
        spCameraType = view.findViewById(R.id.spCameraType)


        spCameraType?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing to change
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val context = context ?: return
                getModelImage(position)?.let {
                    ivModelImg?.setImageDrawable(ContextCompat.getDrawable(context, it))
                }
            }

        }


        pbValidationEmail = view.findViewById(R.id.pbValidationEmail)

    }


    private fun populateMyFields() {

        etSysName?.setText(myCamera?.getName())
        etTelNum?.setText(myCamera?.phoneNum)
        myCamera?.cameraModelPosition?.let { spCameraType?.setSelection(it) }
        myCamera?.lastVisitPicturePath?.let {
            context?.let { context ->
                bitmap = ImageStorageManager.getImageFromInternalStorage(
                    context,
                    "image" + myCamera?.getId()
                )
                bitmap?.let { ibShowPicture?.setImageBitmap(it) }
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
            phone.length in 9..16
        } else false
    }


    private fun saveLastVisitPicture() {
        val context = context ?: return
        bitmap?.let {
            myCamera?.lastVisitPicturePath = ImageStorageManager.saveToInternalStorage(
                context,
                it,
                "image" + myCamera?.getId()
            )
        }
    }

    //open large picture
    private fun openLargePictureDialog() {
        val context = context ?: return
        val bitmap = bitmap ?: return

        val settingsDialog = Dialog(context)
        settingsDialog.window?.requestFeature(FEATURE_NO_TITLE)
        settingsDialog.setContentView(R.layout.fragment_large_picture_video)
        val ibClose = settingsDialog.findViewById<AppCompatImageButton>(R.id.ibClose)
        ibClose?.setOnClickListener {
            settingsDialog.dismiss()
        }

        //keep the proportion of the picture
        val dialogWidth = getScreenWidth(activity) * 5 / 6
        val dialogHeight = bitmap.height * dialogWidth / bitmap.width

        val ivMyCaptureImage =
            settingsDialog.findViewById<AppCompatImageView>(R.id.ivMyCaptureImage)
        //resize the image view according to the size of the bitmap
        ivMyCaptureImage?.layoutParams?.height = dialogHeight
        ivMyCaptureImage?.layoutParams?.width = dialogWidth
        ivMyCaptureImage?.requestLayout()
        ivMyCaptureImage?.setImageBitmap(bitmap)
        settingsDialog.show()
    }

    //return the camera to CamerasFragment and close
    private fun sendResult() {
        val cameraStr = myCamera?.let { convertToGson(it) }
        parentFragmentManager.setFragmentResult(
            CAMERA_EXTRA_SETTINGS_RESULT_KEY,
            Bundle().apply {
                putBoolean(RESULT_IS_OK_KEY, true)
                putString(CAMERA_KEY, cameraStr)
            }
        )
        close()
    }

    private fun sendCancelResult() {
        parentFragmentManager.setFragmentResult(
            CAMERA_EXTRA_SETTINGS_RESULT_KEY,
            Bundle().apply { putBoolean(RESULT_IS_OK_KEY, false) }
        )
        close()
    }

    private fun close() {
        dismiss()
        //remove fragment from stack
        parentFragmentManager.popBackStack()
    }

    //callback of the camera app (returns a thumbnail)
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { result ->
            if (result != null) {
                bitmap = result
                ibShowPicture?.setImageBitmap(bitmap)
            }
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
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
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
