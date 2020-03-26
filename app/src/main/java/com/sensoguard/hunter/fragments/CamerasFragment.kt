package com.sensoguard.hunter.fragments

//import android.support.v4.app.Fragment

//import android.support.design.widget.FloatingActionButton
//import android.support.v4.content.ContextCompat
//import android.support.v7.widget.DividerItemDecoration
//import android.support.v7.widget.LinearLayoutManager
//import android.support.v7.widget.RecyclerView
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sensoguard.hunter.R
import com.sensoguard.hunter.adapters.CamerasAdapter
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.global.*
import com.sensoguard.hunter.interfaces.OnAdapterListener


//import android.R




// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SensorsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SensorsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class SensorsFragment : Fragment(), OnAdapterListener {



    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    //private var listener: OnAdapterListener? = null
    var tvShowLogs:TextView?=null
    var bs: StringBuilder?=null
    private var floatAddSensor: FloatingActionButton?=null
    private var cameras: ArrayList<Camera>? = null
    private var rvSensor: RecyclerView?=null
    private var sensorsAdapter: CamerasAdapter? = null
    private val listenerPref: SharedPreferences.OnSharedPreferenceChangeListener? = null
    private var selectedCamera: Camera? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    //create shared preference handler change
    private val appStateChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
           DETECTORS_LIST_KEY_PREF -> {
               refreshSensorsFromPref()
           }
        }
    }



    private fun initSensorsAdapter() {

        cameras = ArrayList()

        //sensors?.add(Camera(resources.getString(R.string.id_title),resources.getString(R.string.name_title)))

        val itemDecorator= DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.divider)!!)
        rvSensor?.addItemDecoration(itemDecorator)

        sensorsAdapter=activity?.let { adapter ->
            cameras?.let { arr ->
                CamerasAdapter(arr, adapter, this) { _camera ->
                    selectedCamera = _camera
                    selectedCamera?.let { openExtraCameraSettings(it) }
                }
            }
        }
        rvSensor?.adapter=sensorsAdapter
        val layoutManager= LinearLayoutManager(activity)
        rvSensor?.layoutManager=layoutManager

        sensorsAdapter?.notifyDataSetChanged()



    }

    //show dialog of add a new detector
    private fun showDialog() {

        var numSensorsRequest:Int?=null

        //ask before delete extra sensors
        fun askBeforeDeleteExtraSensor() {
            val dialog= AlertDialog.Builder(activity)
                //set message, title, and icon
                .setTitle(activity?.resources?.getString(R.string.remove_extra_sensors)).setMessage(activity?.resources?.getString(R.string.content_delete_extra_sensor)).setIcon(android.R.drawable.ic_menu_delete

                )

                .setPositiveButton(activity?.resources?.getString(R.string.yes)) { dialog, _ ->

                    //remove extra sensors
                    if(numSensorsRequest!=null) {
                        val items = cameras?.listIterator()
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
                    }

                    cameras?.let { sen -> storeSensorsToLocally(sen, activity!!) }
                    dialog.dismiss()
                }


                .setNegativeButton(activity?.resources?.getString(R.string.no)) {
                        dialog, _ -> dialog.dismiss() }.create()
            dialog.show()

        }


        if(activity==null){
            return
        }

        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_new_sensor)

        val etId = dialog.findViewById(R.id.etId) as EditText


        val btnOk = dialog.findViewById(R.id.btnOk) as Button

        btnOk.setOnClickListener {

            if(validIsEmpty(etId) && activity!=null) {


                val sensors= populateSensorsFromLocally()


                try{
                    numSensorsRequest=etId.text.toString().toInt()
                }catch (ex:NumberFormatException){
                    Toast.makeText(this.context, "exception ${ex.message}", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if(numSensorsRequest!=null && numSensorsRequest!! >254){
                    Toast.makeText(this.context, resources.getString(R.string.invalid_mum_sensors), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }


                if(numSensorsRequest!=null) {
                    //add numSensors sensors
                    for (sensorId in 1 until numSensorsRequest!! + 1) {
                        //add it just if not exist
                        if (sensors?.let { it1 -> !isIdExist(it1, sensorId.toString()) }!!) {
                            sensors.add(Camera(sensorId.toString()))
                        }
                    }
                }

                //check if the request of sensors number is smaller then the number of exist
                if(sensors?.size!=null
                    && numSensorsRequest!=null
                    && numSensorsRequest!! < sensors.size){
                    askBeforeDeleteExtraSensor()
                }else if(activity!=null) {
                    sensors?.let { sen -> storeSensorsToLocally(sen, activity!!) }
                }

                dialog.dismiss()
            }
        }

        val btnCancel = dialog.findViewById(R.id.btncn) as Button
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun isIdExist(detectorsArr: ArrayList<Camera>, id: String): Boolean {
        for(item in detectorsArr){
            if(item.getId() == id){
                return true
            }
        }
        return false
    }

    private fun saveNameDetector(detector: Camera) {
        val detectorsArr=populateSensorsFromLocally()
        if (detectorsArr != null) {

            val iteratorList=detectorsArr.listIterator()
            while (iteratorList != null && iteratorList.hasNext()) {
                val detectorItem = iteratorList.next()
                if(detectorItem.getId() == detector.getId()){
                    detector.getName()?.let { detectorItem.setName(it) }
                }
            }

        }
        detectorsArr?.let { activity?.let { context -> storeSensorsToLocally(it, context) } }
    }

    //get the sensors from locally
    private fun populateSensorsFromLocally(): ArrayList<Camera>? {
        val detectors: ArrayList<Camera>?
        val detectorListStr=getStringInPreference(activity,DETECTORS_LIST_KEY_PREF, ERROR_RESP)

        detectors = if(detectorListStr.equals(ERROR_RESP)){
            ArrayList()
        }else {
            detectorListStr?.let { convertJsonToSensorList(it) }
        }
        return detectors
    }


    private fun validIsEmpty(editText: EditText): Boolean {
        var isValid=true

        if (editText.text.isNullOrBlank()) {
            editText.error = resources.getString(R.string.empty_field_error)
            isValid=false
        }

        return isValid
    }

    //update camera
    override fun saveCamera(camera: Camera) {
        val detectorsArr=populateSensorsFromLocally()
        if (detectorsArr != null) {

            val iteratorList=detectorsArr.listIterator()
            while (iteratorList != null && iteratorList.hasNext()) {
                var detectorItem = iteratorList.next()
                if (detectorItem.getId() == camera.getId()) {
                    camera.getName()?.let { detectorItem.setName(it) }
                    camera.getId().let { detectorItem.setId(it) }
                    camera.isArmed().let { detectorItem.setArm(it) }
                    camera.getLatitude().let { detectorItem.setLatitude(it) }
                    camera.getLongtitude().let { detectorItem.setLongtitude(it) }
                    camera.phoneNum.let { detectorItem.phoneNum = it }
                    camera.cameraModel.let { detectorItem.cameraModel = it }
                    camera.cameraModelPosition.let { detectorItem.cameraModelPosition = it }
                    camera.emailAddress.let { detectorItem.emailAddress = it }
                    camera.emailServer.let { detectorItem.emailServer = it }
                    camera.emailPort.let { detectorItem.emailPort = it }
                    camera.isUseSSL.let { detectorItem.isUseSSL = it }
                    camera.password.let { detectorItem.password = it }
                    camera.lastVisitDate.let { detectorItem.lastVisitDate = it }
                    camera.lastVisitPicturePath.let { detectorItem.lastVisitPicturePath = it }
                }
            }

        }
        detectorsArr?.let { activity?.let { context -> storeSensorsToLocally(it, context) } }
    }

    //override fun openLargePictureDialog(imgPath: String) { }

    //ic_edit name (maybe come from adapter)
    override fun saveNameSensor(detector: Camera) {
        saveNameDetector(detector)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sensors, container, false)
        tvShowLogs = view.findViewById(R.id.tvShowLogs)
        rvSensor = view.findViewById(R.id.rvSystemCameras)
//        floatAddSensor = view.findViewById(R.id.floatAddSensor)
//        floatAddSensor?.setOnClickListener {
//
//            openExtraCameraSettings(_camera)
//            //showDialog()
//        }
        bs= StringBuilder()
        return view
    }

    override fun onStart() {
        super.onStart()

        activity?.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)?.registerOnSharedPreferenceChangeListener(appStateChangeListener)

        setFilter()

        initSensorsAdapter()

        refreshSensorsFromPref()

    }

    private fun refreshSensorsFromPref(){
        cameras = ArrayList()
        //sensors?.add(Camera(resources.getString(R.string.id_title),resources.getString(R.string.name_title)))
        val detectorListStr=getStringInPreference(activity,DETECTORS_LIST_KEY_PREF, ERROR_RESP)

        if(detectorListStr.equals(ERROR_RESP)){
            cameras?.add(Camera("1"))
            cameras?.let { storeSensorsToLocally(it, activity!!) }
        }else {

            detectorListStr?.let {
                val temp=convertJsonToSensorList(it)
                temp?.let { tmp -> cameras?.addAll(tmp) }
            }
        }

        sensorsAdapter?.setDetects(cameras)
        sensorsAdapter?.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(usbReceiver)
        activity?.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)?.unregisterOnSharedPreferenceChangeListener(appStateChangeListener)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        //listener?.onFragmentInteraction(uri)
    }

    private fun setFilter() {
        val filter = IntentFilter("handle.read.data")
        activity?.registerReceiver(usbReceiver, filter)
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, arg1: Intent) {
            if (arg1.action == "handle.read.data") {

                //TODO return the code
                val bit=arg1.getIntegerArrayListExtra("data")

                for(item in bit){
                    bs?.append("  ${item.toUByte()}")
                }
                bs?.append("\n")
                tvShowLogs?.text=bs.toString()

            }
        }
    }

    //open fragment dialog to add extra settings
    private fun openExtraCameraSettings(camera: Camera) {

        val fr = CameraExtraSettingsDialogFragment()

        //deliver selected camera to continue add data
        val cameraStr = convertToGson(camera)
        val bdl = Bundle()
        bdl.putString(CAMERA_KEY, cameraStr)
        fr.arguments = bdl
        fr.setTargetFragment(this, TARGET_CAMERA_EXTRA_SETTING_REQUEST_CODE)
        val fm = activity?.supportFragmentManager
        val fragmentTransaction = fm?.beginTransaction()
        fragmentTransaction?.add(R.id.flExtra, fr)
        fragmentTransaction?.commit()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SensorsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SensorsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        //response from camera extra settings
        if (resultCode == Activity.RESULT_OK && requestCode == TARGET_CAMERA_EXTRA_SETTING_REQUEST_CODE) {
            val cameraStr = intent?.extras?.getString(CAMERA_KEY, null)
            cameraStr?.let { selectedCamera = convertJsonToSensor(cameraStr) }
            selectedCamera?.let { saveCamera(it) }
            Log.d("", "")
        }
    }
}
