package com.sensoguard.trailmanager.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.adapters.CamerasAdapter
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.global.CAMERA_EXTRA_SETTINGS_RESULT_KEY
import com.sensoguard.trailmanager.global.CAMERA_KEY
import com.sensoguard.trailmanager.global.COMMANDS_ACTION_TYPE
import com.sensoguard.trailmanager.global.DELETE_ACTION_TYPE
import com.sensoguard.trailmanager.global.DETECTORS_LIST_KEY_PREF
import com.sensoguard.trailmanager.global.EDIT_ACTION_TYPE
import com.sensoguard.trailmanager.global.RESULT_IS_OK_KEY
import com.sensoguard.trailmanager.global.SHARED_PREF_FILE_NAME
import com.sensoguard.trailmanager.global.convertJsonToSensor
import com.sensoguard.trailmanager.global.convertToGson
import com.sensoguard.trailmanager.global.getCamerasFromLocally
import com.sensoguard.trailmanager.global.storeSensorsToLocally


/**
 * The list of the cameras (main screen)
 */
class CamerasFragment : Fragment() {

    private var floatAddCamera: FloatingActionButton? = null
    private var cameras: ArrayList<Camera> = ArrayList()
    private var rvSensor: RecyclerView? = null
    private var camerasAdapter: CamerasAdapter? = null
    private var selectedCamera: Camera? = null

    //create shared preference handler change
    private val appStateChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == DETECTORS_LIST_KEY_PREF) {
                refreshCamerasFromPref()
            }
        }


    private fun initCamerasAdapter() {

        val context = requireContext()
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(context, R.drawable.divider)
            ?.let { itemDecorator.setDrawable(it) }
        rvSensor?.addItemDecoration(itemDecorator)

        camerasAdapter = CamerasAdapter(cameras) { camera: Camera, typeAction: Int ->
            selectedCamera = camera
            when (typeAction) {
                EDIT_ACTION_TYPE -> {
                    floatAddCamera?.visibility = View.INVISIBLE
                    openExtraCameraSettings(camera)
                }

                DELETE_ACTION_TYPE -> {
                    deleteCamera()
                    refreshCamerasFromPref()
                }

                COMMANDS_ACTION_TYPE -> {
                    if (camera.phoneNum.isNullOrEmpty()) {
                        Toast.makeText(
                            this.context,
                            resources.getString(R.string.order_cannot_sent),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        floatAddCamera?.visibility = View.INVISIBLE
                        openExtraCameraCommands(camera)
                    }
                }
            }
        }
        rvSensor?.adapter = camerasAdapter
        rvSensor?.layoutManager = LinearLayoutManager(activity)
    }

    //update camera
    private fun saveCamera(camera: Camera) {
        val context = context ?: return
        val detectorsArr = getCamerasFromLocally(context) ?: return
        for (detectorItem in detectorsArr) {
            if (detectorItem.getId() == camera.getId()) {
                camera.getName()?.let { detectorItem.setName(it) }
                detectorItem.setId(camera.getId())
                detectorItem.setArm(camera.isArmed())
                detectorItem.setLatitude(camera.getLatitude())
                detectorItem.setLongitude(camera.getLongitude())
                detectorItem.phoneNum = camera.phoneNum
                detectorItem.cameraModel = camera.cameraModel
                detectorItem.cameraModelPosition = camera.cameraModelPosition
                detectorItem.emailAddress = camera.emailAddress
                detectorItem.emailServer = camera.emailServer
                detectorItem.emailPort = camera.emailPort
                detectorItem.isUseSSL = camera.isUseSSL
                detectorItem.password = camera.password
                detectorItem.lastVisitDate = camera.lastVisitDate
                detectorItem.lastVisitPicturePath = camera.lastVisitPicturePath
            }
        }
        storeSensorsToLocally(detectorsArr, context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cameras, container, false)
        rvSensor = view.findViewById(R.id.rvSystemCameras)
        floatAddCamera = view.findViewById(R.id.floatAddSensor)
        floatAddCamera?.setOnClickListener {
            addCamera()
            refreshCamerasFromPref()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCamerasAdapter()
        requireActivity().supportFragmentManager.addOnBackStackChangedListener(backStackListener)

        //response from camera extra settings (the dialog is added to the fragment manager of the activity)
        requireActivity().supportFragmentManager.setFragmentResultListener(
            CAMERA_EXTRA_SETTINGS_RESULT_KEY,
            viewLifecycleOwner
        ) { _, result ->
            floatAddCamera?.visibility = View.VISIBLE
            if (result.getBoolean(RESULT_IS_OK_KEY)) {
                result.getString(CAMERA_KEY)?.let { selectedCamera = convertJsonToSensor(it) }
                selectedCamera?.let { saveCamera(it) }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        activity?.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
            ?.registerOnSharedPreferenceChangeListener(appStateChangeListener)

        refreshCamerasFromPref()

    }

    private fun refreshCamerasFromPref() {
        val context = context ?: return
        getCamerasFromLocally(context)?.let { cameras = ArrayList(it) }
        camerasAdapter?.setDetects(cameras)
    }

    override fun onStop() {
        super.onStop()
        activity?.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
            ?.unregisterOnSharedPreferenceChangeListener(appStateChangeListener)
    }

    override fun onDestroyView() {
        activity?.supportFragmentManager?.removeOnBackStackChangedListener(backStackListener)
        super.onDestroyView()
    }

    //show the add button only when no dialog is open
    private val backStackListener = FragmentManager.OnBackStackChangedListener {
        val fm = activity?.supportFragmentManager ?: return@OnBackStackChangedListener
        //if the dialog is close then the add button is visible
        floatAddCamera?.visibility =
            if (fm.backStackEntryCount == 0) View.VISIBLE else View.INVISIBLE
    }

    //open fragment dialog to add extra settings
    private fun openExtraCameraSettings(camera: Camera) {

        val fr = CameraExtraSettingsDialogFragment()

        //deliver selected camera to continue add data
        fr.arguments = Bundle().apply { putString(CAMERA_KEY, convertToGson(camera)) }
        activity?.supportFragmentManager?.beginTransaction()
            ?.addToBackStack(fr.tag)
            ?.add(R.id.flExtra, fr)
            ?.commit()
    }


    //open fragment dialog to make commands
    private fun openExtraCameraCommands(camera: Camera) {

        val fr = CameraCommandsDialogFragment()
        //deliver selected camera to continue add data
        fr.arguments = Bundle().apply { putString(CAMERA_KEY, convertToGson(camera)) }
        activity?.supportFragmentManager?.beginTransaction()
            ?.addToBackStack(fr.tag)
            ?.add(R.id.flExtra, fr, "CameraCommandsDialogFragment")
            ?.commit()
    }

    //add default camera (the id is the next number after the last camera)
    private fun addCamera() {
        val context = context ?: return
        val cameras = getCamerasFromLocally(context) ?: return

        val lastId = if (cameras.isNotEmpty()) cameras[cameras.size - 1].getId() else "0"
        try {
            cameras.add(Camera(lastId.toInt() + 1))
            storeSensorsToLocally(cameras, context)
        } catch (ex: NumberFormatException) {
            Log.w("CamerasFragment", "invalid camera id: $lastId", ex)
        }
    }

    //delete camera
    private fun deleteCamera() {
        val context = context ?: return
        val cameras = getCamerasFromLocally(context) ?: return
        cameras.removeAll { it.getId() == selectedCamera?.getId() }
        storeSensorsToLocally(cameras, context)
    }
}
