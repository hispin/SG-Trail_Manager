package com.sensoguard.hunter.fragments

//import android.support.v4.app.Fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.hunter.R
import com.sensoguard.hunter.adapters.AlarmAdapter
import com.sensoguard.hunter.classes.Alarm
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.global.*
import com.sensoguard.hunter.interfaces.OnAdapterListener
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AlarmLogFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AlarmLogFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class AlarmLogFragment : Fragment(), OnAdapterListener {
    override fun saveCamera(detector: Camera) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveNameSensor(detector: Camera) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var isAdapterSorted: Boolean = false
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var myAlarms: ArrayList<Alarm>? = null
    private var mySortedAlarms: ArrayList<Alarm>? = null
    private var rvAlarm:RecyclerView?=null
    private var alarmAdapter: AlarmAdapter?=null
    private var btnCsv: Button?=null
    private var viewBetweenContainer: View? = null
    private var btnFilterSystem: Button? = null
    private var btnFilterDateTime: Button? = null
    private var cbIsSelected: CheckBox? = null
    private var ibDeleteSelectedItems: ImageButton? = null



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
            ALARM_LIST_KEY_PREF -> {
                refreshAlarmsFromPref()
            }
        }
    }


    private fun initAlarmsGridAdapter() {

        viewBetweenContainer?.visibility = View.INVISIBLE

        if (rvAlarm != null && rvAlarm?.itemDecorationCount!! > 0) {
            rvAlarm?.removeItemDecorationAt(0)
        }

        myAlarms = ArrayList()


        alarmAdapter = activity?.let { adapter ->
            myAlarms?.let { arr ->
                AlarmAdapter(
                    arr, adapter,
                    R.layout.item_alarm_grid, true
                ) { alarm, type ->
                    if (type == 1) {
                        openLargePictureDialog(alarm)
                    } else if (type == 2) {
                        alarm.imgsPath?.let { shareImage(it) }
                    }
                }
            }
        }
        rvAlarm?.adapter = alarmAdapter
        val layoutManager = androidx.recyclerview.widget.GridLayoutManager(activity, 2)
        rvAlarm?.layoutManager = layoutManager

        alarmAdapter?.notifyDataSetChanged()

    }

    private fun initAlarmsAdapter() {

        viewBetweenContainer?.visibility = View.VISIBLE

        myAlarms = ArrayList()
        //alarms?.add(Alarm("ID", "NAME", "TYPE", "TIME", false, -1))
        val itemDecorator= DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(
            ContextCompat.getDrawable(
                context!!,
                R.drawable.divider_alarm_log
            )!!
        )
        rvAlarm?.addItemDecoration(itemDecorator)

        alarmAdapter=activity?.let { adapter ->
            myAlarms?.let { arr ->
                AlarmAdapter(
                    arr,
                    adapter,
                    R.layout.item_alarm,
                    false
                ) { alarm, type ->
                    if (type == 1) {
                        openLargePictureDialog(alarm)
                    } else if (type == 2) {
                        try {
                            alarm.imgsPath?.let { shareImage(it) }
                        } catch (ex: java.lang.Exception) {
                            ex.printStackTrace()
                            Toast.makeText(
                                activity,
                                resources.getString(R.string.no_photo),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }
            }
        }
        rvAlarm?.adapter=alarmAdapter
        val layoutManager= LinearLayoutManager(activity)
        rvAlarm?.layoutManager=layoutManager

        alarmAdapter?.notifyDataSetChanged()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_alarm_log, container, false)

        viewBetweenContainer = view.findViewById(R.id.viewBetweenContainer)
        rvAlarm=view.findViewById(R.id.rvAlarm)
        btnFilterSystem = view.findViewById(R.id.btnFilterSystem)
        btnFilterSystem?.setOnClickListener {

            //clear if selected
            clearSelection()

            this@AlarmLogFragment.context?.let { it1 ->
                ContextCompat.getColor(
                    it1, R.color.green2
                )
            }?.let { it2 -> it.setBackgroundColor(it2) }

            this@AlarmLogFragment.context?.let { it1 ->
                ContextCompat.getColor(
                    it1, R.color.white
                )
            }?.let { it2 -> (it as Button).setTextColor(it2) }

            btnFilterSystem?.isEnabled = false
            btnFilterDateTime?.isEnabled = false

            openSortByType(SORT_BY_SYSTEM_KEY, SORT_BY_SYSTEM_REQUEST_CODE)
        }
        btnFilterDateTime = view.findViewById(R.id.btnFilterDateTime)
        btnFilterDateTime?.setOnClickListener {

            //clear if selected
            clearSelection()

            this@AlarmLogFragment.context?.let { it1 ->
                ContextCompat.getColor(
                    it1, R.color.green2
                )
            }?.let { it2 -> it.setBackgroundColor(it2) }

            this@AlarmLogFragment.context?.let { it1 ->
                ContextCompat.getColor(
                    it1, R.color.white
                )
            }?.let { it2 -> (it as Button).setTextColor(it2) }

            btnFilterSystem?.isEnabled = false
            btnFilterDateTime?.isEnabled = false

            openSortByType(SORT_BY_DATETIME_KEY, SORT_PICK_DATE_TIME_REQUEST_CODE)
        }

        cbIsSelected = view.findViewById(R.id.cbIsSelected)
        cbIsSelected?.setOnCheckedChangeListener { _, isChecked ->
            if (isAdapterSorted) {
                mySortedAlarms?.let { toggleItemSelected(it, isChecked) }
            } else {
                myAlarms?.let { toggleItemSelected(it, isChecked) }
            }
        }

        ibDeleteSelectedItems = view.findViewById(R.id.ibDeleteSelectedItems)
        ibDeleteSelectedItems?.setOnClickListener {

            var alarmCounter = 0
            if (isAdapterSorted) {
                mySortedAlarms?.let { it1 -> alarmCounter = getCountItemSelected(it1) }
            } else {
                myAlarms?.let { it1 -> alarmCounter = getCountItemSelected(it1) }
            }
            if (alarmCounter > 0) {
                showDeleteDialog(alarmCounter)
            } else {
                Toast.makeText(
                    activity,
                    resources.getString(R.string.no_selected_alarms),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }

        isAdapterSorted = false

        // Inflate the layout for this fragment
        return view
    }

    //clear the selection of the sorted and normal array
    private fun clearSelection() {
        cbIsSelected?.isChecked = false
        mySortedAlarms?.let { toggleItemSelected(it, false) }
        myAlarms?.let { toggleItemSelected(it, false) }
    }

    private fun setFilter() {
        val filter = IntentFilter(DETECT_ALARM_KEY)
        activity?.registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(receiver)
    }


    override fun onResume() {
        super.onResume()
        //display the alarm log according to view type
        val alarmDisplay = getStringInPreference(this.context, ALARM_DISPLAY_KEY, "list")
        if (alarmDisplay != null && (alarmDisplay == "list")) {
            initAlarmsAdapter()
        } else if (alarmDisplay != null && (alarmDisplay == "grid")) {
            initAlarmsGridAdapter()
        }

        refreshAlarmsFromPref()
    }

    override fun onStart() {
        super.onStart()

        setFilter()

        activity?.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)?.registerOnSharedPreferenceChangeListener(appStateChangeListener)

    }

    override fun onStop() {
        super.onStop()
        activity?.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)?.unregisterOnSharedPreferenceChangeListener(appStateChangeListener)
    }

    private fun refreshAlarmsFromPref(){
        isAdapterSorted = false
        myAlarms = ArrayList()

        val _alarms=populateAlarmsFromLocally()

        //show only alarm log from email
        val iteratorList = _alarms?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val item = iteratorList.next()
            if (item.isCameFromEmail) {
                myAlarms?.add(item)
            }
        }

        myAlarms?.let { myAlarms ->
            this.myAlarms = ArrayList(myAlarms.sortedWith(compareByDescending { it.timeInMillis }))
            //myAlarms?.let { alarms?.addAll(it) }

            alarmAdapter?.setDetects(this.myAlarms)
            alarmAdapter?.notifyDataSetChanged()
        }

    }

    //get the alarms from locally
    private fun populateAlarmsFromLocally(): ArrayList<Alarm>?  {
        val alarms: ArrayList<Alarm>?
        val alarmListStr= getStringInPreference(context, ALARM_LIST_KEY_PREF, ERROR_RESP)

        alarms = if(alarmListStr.equals(ERROR_RESP)){
            ArrayList()
        }else {
            alarmListStr?.let { convertJsonToAlarmList(it) }
        }
        return alarms
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryWarningFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AlarmLogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, inn: Intent) {
            //accept currentAlarm
            if (inn.action == DETECT_ALARM_KEY) {

                Log.d("testLogAlarm", "accept alarm")

                refreshAlarmsFromPref()


            }
        }
    }

    //open large picture
    private fun openLargePictureDialog(alarm: Alarm) {
        if (alarm.imgsPath != null && alarm.imgsPath!!.endsWith("mp4")) {
            //from external storage(ACTION_VIDEO_KEY, alarm.imgsPath, 0)
        } else {
            openLargePictureVideoByType(ACTION_PICTURE_KEY, alarm.imgsPath, 0)
        }
    }


    //share image from selected alarm
    private fun shareImage(path: String) {
        val uriFile = Uri.parse(path)

        if (uriFile.path == null) {
            return
        }

        val file = File(uriFile.path!!)

        val uri = this.context?.let {
            FileProvider.getUriForFile(
                it,
                "${activity?.packageName}.contentprovider", //(use your app signature + ".provider" )
                file
            )
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(intent, "Share Image"))
    }

    //open fragment dialog to sort the list of alarm log
    private fun openSortByType(type: Int, requestCode: Int) {

        val fr = SystemSortDialogFragment()

        //deliver selected camera to continue add data
        //val cameraStr = convertToGson(camera)
        val bdl = Bundle()
        bdl.putInt(SORT_TYPE_KEY, type)
        fr.arguments = bdl
        fr.setTargetFragment(this, requestCode)
        val fm = activity?.supportFragmentManager
        val fragmentTransaction = fm?.beginTransaction()
        fragmentTransaction?.add(R.id.flSortBySystemCamera, fr)
        fragmentTransaction?.commit()
    }

    //open fragment dialog to see a large picture or video
    private fun openLargePictureVideoByType(type: Int, imgPath: String?, requestCode: Int) {


        if (imgPath == null) {
            Toast.makeText(activity, resources.getString(R.string.no_photo), Toast.LENGTH_LONG)
                .show()
            return
        }

        val imgFile = File(imgPath)

        //from internal storage
        //val imgFile = File(context?.filesDir, path)

        if (!imgFile.exists()) {
            Toast.makeText(activity, resources.getString(R.string.no_photo), Toast.LENGTH_LONG)
                .show()
            return
        }

        val fr = LargePictureVideoDialogFragment()

        //deliver selected camera to continue add data
        //val cameraStr = convertToGson(camera)
        val bdl = Bundle()
        bdl.putInt(ACTION_TYPE_KEY, type)
        bdl.putString(IMAGE_PATH_KEY, imgPath)
        fr.arguments = bdl
        fr.setTargetFragment(this, requestCode)
        val fm = activity?.supportFragmentManager
        fm?.let { fr.show(it, "LargePictureVideoDialogFragment") }
//        val fragmentTransaction = fm?.beginTransaction()
//        fragmentTransaction?.add(R.id.flSortBySystemCamera, fr)
//        fragmentTransaction?.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        //response from camera extra settings


        btnFilterSystem?.isEnabled = true
        btnFilterDateTime?.isEnabled = true

        //change the color of the button
        this@AlarmLogFragment.context?.let { it1 ->
            ContextCompat.getColor(
                it1, R.color.gray11
            )
        }?.let { it2 -> btnFilterSystem?.setBackgroundColor(it2) }


        this@AlarmLogFragment.context?.let { it1 ->
            ContextCompat.getColor(
                it1, R.color.black
            )
        }?.let { it2 -> (btnFilterSystem as Button).setTextColor(it2) }

        //change the color of the button
        this@AlarmLogFragment.context?.let { it1 ->
            ContextCompat.getColor(
                it1, R.color.gray11
            )
        }?.let { it2 -> btnFilterDateTime?.setBackgroundColor(it2) }

        this@AlarmLogFragment.context?.let { it1 ->
            ContextCompat.getColor(
                it1, R.color.black
            )
        }?.let { it2 -> (btnFilterDateTime as Button).setTextColor(it2) }

        if (requestCode == SORT_BY_SYSTEM_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val cameraStr = intent?.extras?.getString(CAMERA_KEY, null)
                var myCameras: ArrayList<Camera>? = null
                cameraStr?.let { myCameras = convertJsonToSensorList(cameraStr) }
                sortAlarm(myCameras)
            }
        } else if (requestCode == SORT_PICK_DATE_TIME_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                //get the start date and end date for sorting
                try {
                    val fromCalendar: Calendar =
                        intent?.getSerializableExtra("fromCalendar") as Calendar
                    fromCalendar.add(Calendar.DATE, -1)
                    //val fromDateStr=activity?.let { it1 -> getStringFromCalendar(fromCalendar, "dd/MM/yy kk:mm:ss", it1) }
                    val toCalendar: Calendar = intent.getSerializableExtra("toCalendar") as Calendar

                    sortAlarm(fromCalendar, toCalendar)
                    isAdapterSorted = true
                    //val toDateStr=activity?.let { it1 -> getStringFromCalendar(toCalendar, "dd/MM/yy kk:mm:ss", it1) }
                    //Log.d("testCalendar",fromDateStr)
                    //Log.d("testCalendar",toDateStr)
                } catch (ex: Exception) {
                    Toast.makeText(activity, resources.getString(R.string.error), Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    //sort the alarm by date time
    private fun sortAlarm(fromCalendar: Calendar, toCalendar: Calendar) {
        mySortedAlarms = ArrayList()
        val iteratorList = myAlarms?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val item = iteratorList.next()
            if (item.timeInMillis != null
                && item.timeInMillis!! <= toCalendar.timeInMillis
                && item.timeInMillis!! >= fromCalendar.timeInMillis
            )
                mySortedAlarms?.add(item)
        }
        alarmAdapter?.setDetects(mySortedAlarms)
        alarmAdapter?.notifyDataSetChanged()
    }

    //sort the alarm by sorter camera
    private fun sortAlarm(myCameras: ArrayList<Camera>?) {
        mySortedAlarms = ArrayList()
        val iteratorList = myAlarms?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val item = iteratorList.next()
            if (isAlarmSorted(item, myCameras))
                mySortedAlarms?.add(item)
        }
        alarmAdapter?.setDetects(mySortedAlarms)
        alarmAdapter?.notifyDataSetChanged()
    }

    //check if the camera of alarm is sorted
    private fun isAlarmSorted(itemP: Alarm, myCameras: ArrayList<Camera>?): Boolean {

        val iteratorList = myCameras?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val item = iteratorList.next()
            if (itemP.id.equals(item.getId()) && item.isSorted) {
                return true
            }
        }
        return false
    }

    //toggle selected/unselected alarms
    private fun toggleItemSelected(alarms: ArrayList<Alarm>, isSelected: Boolean) {
        val iteratorList = alarms.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val item = iteratorList.next()
            item.isReadyToDelete = isSelected
            alarmAdapter?.setDetects(alarms)
            alarmAdapter?.notifyDataSetChanged()
        }
    }

    //get the counter of selected alarms
    private fun getCountItemSelected(alarms: ArrayList<Alarm>): Int {
        val iteratorList = alarms.listIterator()
        var counter = 0
        while (iteratorList != null && iteratorList.hasNext()) {
            val item = iteratorList.next()
            if (item.isReadyToDelete) {
                counter++
            }

        }
        return counter
    }

    //delete the selected alarms
    private fun deleteItemSelected(alarms: ArrayList<Alarm>): Int {
        val iteratorList = alarms.listIterator()
        var counter = 0
        while (iteratorList != null && iteratorList.hasNext()) {
            val item = iteratorList.next()
            if (item.isReadyToDelete) {
                iteratorList.remove()
                counter++
            }

        }
        return counter
    }

    //show dialog before delete alarms
    private fun showDeleteDialog(alarmCounter: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(alarmCounter.toString() + " " + context!!.resources.getString(R.string.selected_alarms))
        val yes = context!!.resources.getString(R.string.yes)
        val no = context!!.resources.getString(R.string.no)
        builder.setMessage(context!!.resources.getString(R.string.do_you_realy_want_delete_selected_alarm))
            .setCancelable(false)
        builder.setPositiveButton(yes) { dialog, which ->

            //delete from main array and also from sort array
            myAlarms?.let { deleteItemSelected(it) }
            mySortedAlarms?.let { deleteItemSelected(it) }
            //save the changing in shared preference
            if (context != null && myAlarms != null && myAlarms?.size != null && myAlarms?.size!! > 0) {
                myAlarms?.let { storeAlarmsToLocally(it, context!!) }
            }

            //refresh the system
            if (isAdapterSorted) {
                clearSelection()
                alarmAdapter?.setDetects(mySortedAlarms)
                alarmAdapter?.notifyDataSetChanged()
            } else {
                refreshAlarmsFromPref()
            }

            dialog.dismiss()
        }


        // Display a negative button on alert dialog
        builder.setNegativeButton(no) { dialog, which ->
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }
}
