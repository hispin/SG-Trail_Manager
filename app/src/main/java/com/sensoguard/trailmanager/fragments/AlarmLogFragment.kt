package com.sensoguard.trailmanager.fragments

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.adapters.AlarmAdapter
import com.sensoguard.trailmanager.classes.Alarm
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.global.ALARM_DISPLAY_KEY
import com.sensoguard.trailmanager.global.ALARM_LIST_KEY_PREF
import com.sensoguard.trailmanager.global.CAMERA_KEY
import com.sensoguard.trailmanager.global.CAMERA_SORTED
import com.sensoguard.trailmanager.global.DATE_SORTED
import com.sensoguard.trailmanager.global.DETECT_ALARM_KEY
import com.sensoguard.trailmanager.global.NO_SORTED
import com.sensoguard.trailmanager.global.RESULT_IS_OK_KEY
import com.sensoguard.trailmanager.global.SHARED_PREF_FILE_NAME
import com.sensoguard.trailmanager.global.SORT_BY_DATETIME_KEY
import com.sensoguard.trailmanager.global.SORT_BY_SYSTEM_KEY
import com.sensoguard.trailmanager.global.SORT_BY_SYSTEM_RESULT_KEY
import com.sensoguard.trailmanager.global.SORT_FROM_TIME_KEY
import com.sensoguard.trailmanager.global.SORT_PICK_DATE_TIME_RESULT_KEY
import com.sensoguard.trailmanager.global.SORT_TO_TIME_KEY
import com.sensoguard.trailmanager.global.SORT_TYPE_KEY
import com.sensoguard.trailmanager.global.convertJsonToSensorList
import com.sensoguard.trailmanager.global.getAlarmsFromLocally
import com.sensoguard.trailmanager.global.getStringInPreference
import com.sensoguard.trailmanager.global.storeAlarmsToLocally
import java.io.File


/**
 * The log of the alarms (that came from emails), can be filtered by cameras or by dates
 */
class AlarmLogFragment : Fragment() {

    private var typeOfSorted: Int = NO_SORTED
    private var myAlarms: ArrayList<Alarm> = ArrayList()
    private var mySortedAlarms: ArrayList<Alarm>? = null
    private var rvAlarm: RecyclerView? = null
    private var alarmAdapter: AlarmAdapter? = null
    private var viewBetweenContainer: View? = null
    private var btnFilterSystem: Button? = null
    private var btnFilterDateTime: Button? = null
    private var cbIsSelected: CheckBox? = null
    private var ibDeleteSelectedItems: ImageButton? = null
    private var tvReset: TextView? = null

    private var mySortedCameras: ArrayList<Camera>? = null
    private var fromTimeInMillis: Long? = null
    private var toTimeInMillis: Long? = null

    //the alarms that are shown now (sorted or all)
    private val shownAlarms: ArrayList<Alarm>?
        get() = if (typeOfSorted == DATE_SORTED || typeOfSorted == CAMERA_SORTED) mySortedAlarms else myAlarms

    //create shared preference handler change
    private val appStateChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == ALARM_LIST_KEY_PREF) {
            refreshAlarmsFromPref()
        }
    }

    //the action of a click on an alarm
    private fun onAlarmClick(alarm: Alarm, type: Int) {
        if (type == 1) {
            openLargePictureDialog(alarm)
        } else if (type == 2) {
            try {
                alarm.imgsPath?.let { shareImage(it) }
            } catch (ex: IllegalArgumentException) {
                Log.w("AlarmLogFragment", "share image failed", ex)
                Toast.makeText(activity, resources.getString(R.string.no_photo), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun initAlarmsGridAdapter() {
        val context = context ?: return

        viewBetweenContainer?.visibility = View.INVISIBLE

        if ((rvAlarm?.itemDecorationCount ?: 0) > 0) {
            rvAlarm?.removeItemDecorationAt(0)
        }

        alarmAdapter =
            AlarmAdapter(ArrayList(), context, R.layout.item_alarm_grid, true) { alarm, type ->
                onAlarmClick(alarm, type)
        }
        rvAlarm?.adapter = alarmAdapter
        rvAlarm?.layoutManager = GridLayoutManager(context, 2)
    }

    private fun initAlarmsAdapter() {
        val context = context ?: return

        viewBetweenContainer?.visibility = View.VISIBLE

        if ((rvAlarm?.itemDecorationCount ?: 0) == 0) {
            val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            ContextCompat.getDrawable(context, R.drawable.divider_alarm_log)
                ?.let { itemDecorator.setDrawable(it) }
            rvAlarm?.addItemDecoration(itemDecorator)
        }

        alarmAdapter =
            AlarmAdapter(ArrayList(), context, R.layout.item_alarm, false) { alarm, type ->
                onAlarmClick(alarm, type)
        }
        rvAlarm?.adapter = alarmAdapter
        rvAlarm?.layoutManager = LinearLayoutManager(context)
    }

    //mark the filter button as selected and open the filter dialog
    private fun onFilterClick(button: View, sortType: Int) {
        //clear if selected
        clearSelection()

        context?.let {
            button.setBackgroundColor(ContextCompat.getColor(it, R.color.green2))
            (button as Button).setTextColor(ContextCompat.getColor(it, R.color.white))
        }

        btnFilterSystem?.isEnabled = false
        btnFilterDateTime?.isEnabled = false

        openSortByType(sortType)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_alarm_log, container, false)

        viewBetweenContainer = view.findViewById(R.id.viewBetweenContainer)
        rvAlarm = view.findViewById(R.id.rvAlarm)
        btnFilterSystem = view.findViewById(R.id.btnFilterSystem)
        btnFilterSystem?.setOnClickListener { onFilterClick(it, SORT_BY_SYSTEM_KEY) }
        btnFilterDateTime = view.findViewById(R.id.btnFilterDateTime)
        btnFilterDateTime?.setOnClickListener { onFilterClick(it, SORT_BY_DATETIME_KEY) }

        cbIsSelected = view.findViewById(R.id.cbSelectAll)
        cbIsSelected?.setOnCheckedChangeListener { _, isChecked ->
            shownAlarms?.let { toggleItemSelected(it, isChecked) }
        }

        ibDeleteSelectedItems = view.findViewById(R.id.ibDeleteSelectedItems)
        ibDeleteSelectedItems?.setOnClickListener {
            val alarmCounter = shownAlarms?.count { it.isReadyToDelete } ?: 0
            if (alarmCounter > 0) {
                showDeleteDialog(alarmCounter)
            } else {
                Toast.makeText(
                    activity,
                    resources.getString(R.string.no_selected_alarms),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        tvReset = view.findViewById(R.id.tvReset)
        tvReset?.let { Linkify.addLinks(it, Linkify.WEB_URLS) }
        tvReset?.setOnClickListener {
            typeOfSorted = NO_SORTED
            refreshAlarmsFromPref()
        }
        typeOfSorted = NO_SORTED

        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //results of the filter dialog (it is added to the fragment manager of the activity)
        val fm = requireActivity().supportFragmentManager
        fm.setFragmentResultListener(SORT_BY_SYSTEM_RESULT_KEY, viewLifecycleOwner) { _, result ->
            resetFilterButtons()
            if (result.getBoolean(RESULT_IS_OK_KEY)) {
                result.getString(CAMERA_KEY)?.let { mySortedCameras = convertJsonToSensorList(it) }
                if (mySortedCameras != null) {
                    typeOfSorted = CAMERA_SORTED
                    refreshAlarmsFromPref()
                }
            }
        }
        fm.setFragmentResultListener(
            SORT_PICK_DATE_TIME_RESULT_KEY,
            viewLifecycleOwner
        ) { _, result ->
            resetFilterButtons()
            if (result.getBoolean(RESULT_IS_OK_KEY)) {
                //get the start date and end date for sorting
                fromTimeInMillis = result.getLong(SORT_FROM_TIME_KEY)
                toTimeInMillis = result.getLong(SORT_TO_TIME_KEY)
                typeOfSorted = DATE_SORTED
                refreshAlarmsFromPref()
            }
        }
    }

    //clear the selection of the sorted and normal array
    private fun clearSelection() {
        cbIsSelected?.isChecked = false
        mySortedAlarms?.let { toggleItemSelected(it, false) }
        toggleItemSelected(myAlarms, false)
    }

    override fun onResume() {
        super.onResume()
        //display the alarm log according to view type
        when (getStringInPreference(this.context, ALARM_DISPLAY_KEY, "list")) {
            "list" -> initAlarmsAdapter()
            "grid" -> initAlarmsGridAdapter()
        }

        refreshAlarmsFromPref()
    }

    override fun onStart() {
        super.onStart()

        activity?.let {
            ContextCompat.registerReceiver(
                it,
                receiver,
                IntentFilter(DETECT_ALARM_KEY),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        activity?.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
            ?.registerOnSharedPreferenceChangeListener(appStateChangeListener)

    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(receiver)
        activity?.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
            ?.unregisterOnSharedPreferenceChangeListener(appStateChangeListener)
    }

    private fun refreshAlarmsFromPref() {
        val context = context ?: return

        //show only alarm log from email
        val alarmsFromEmail =
            getAlarmsFromLocally(context)?.filter { it.isCameFromEmail } ?: emptyList()
        myAlarms = ArrayList(alarmsFromEmail.sortedWith(compareByDescending { it.timeInMillis }))

        when (typeOfSorted) {
            DATE_SORTED -> sortByDateAlarm()
            CAMERA_SORTED -> sortByCamerasAlarm()
        }
        alarmAdapter?.setDetects(shownAlarms)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, inn: Intent) {
            //accept currentAlarm
            if (inn.action == DETECT_ALARM_KEY) {
                refreshAlarmsFromPref()
            }
        }
    }

    //open large picture (only pictures, the videos are not supported)
    private fun openLargePictureDialog(alarm: Alarm) {
        val imgPath = alarm.imgsPath
        if (imgPath?.endsWith("mp4") == true) {
            return
        }
        if (imgPath == null || !File(imgPath).exists()) {
            Toast.makeText(activity, resources.getString(R.string.no_photo), Toast.LENGTH_LONG)
                .show()
        }
    }


    //share image from selected alarm
    private fun shareImage(path: String) {
        val context = context ?: return
        val filePath = path.toUri().path ?: return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.contentprovider",
            File(filePath)
        )

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, resources.getString(R.string.share_image)))
    }

    //open fragment dialog to sort the list of alarm log
    private fun openSortByType(type: Int) {
        val fr = SystemSortDialogFragment()
        fr.arguments = Bundle().apply { putInt(SORT_TYPE_KEY, type) }
        activity?.supportFragmentManager?.beginTransaction()
            ?.add(R.id.flSortBySystemCamera, fr)
            ?.commit()
    }

    //return the filter buttons to the normal state
    private fun resetFilterButtons() {
        btnFilterSystem?.isEnabled = true
        btnFilterDateTime?.isEnabled = true

        val context = context ?: return
        val gray = ContextCompat.getColor(context, R.color.gray11)
        val black = ContextCompat.getColor(context, R.color.black)
        btnFilterSystem?.setBackgroundColor(gray)
        btnFilterSystem?.setTextColor(black)
        btnFilterDateTime?.setBackgroundColor(gray)
        btnFilterDateTime?.setTextColor(black)
    }

    //sort the alarm by date time
    private fun sortByDateAlarm() {
        val from = fromTimeInMillis ?: return
        val to = toTimeInMillis ?: return
        mySortedAlarms = ArrayList(myAlarms.filter { alarm ->
            alarm.timeInMillis?.let { it in from..to } == true
        })
    }

    //sort the alarm by sorted cameras
    private fun sortByCamerasAlarm() {
        val sortedCameras = mySortedCameras ?: return
        mySortedAlarms = ArrayList(myAlarms.filter { alarm ->
            sortedCameras.any { camera -> alarm.id == camera.getId() && camera.isSorted }
        })
    }

    //toggle selected/unselected alarms
    private fun toggleItemSelected(alarms: ArrayList<Alarm>, isSelected: Boolean) {
        alarms.forEach { it.isReadyToDelete = isSelected }
        alarmAdapter?.setDetects(alarms)
    }

    //show dialog before delete alarms
    private fun showDeleteDialog(alarmCounter: Int) {
        val context = context ?: return
        AlertDialog.Builder(context)
            .setTitle(
                resources.getQuantityString(
                    R.plurals.selected_alarms_title,
                    alarmCounter,
                    alarmCounter
                )
            )
            .setMessage(getString(R.string.do_you_really_want_delete_selected_alarm))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->

                //delete from main array and also from sort array
                myAlarms.removeAll { it.isReadyToDelete }
                mySortedAlarms?.removeAll { it.isReadyToDelete }
                //save the changing in shared preference
                storeAlarmsToLocally(myAlarms, context)

                clearSelection()

                refreshAlarmsFromPref()

                dialog.dismiss()
            }
            // Display a negative button on alert dialog
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
