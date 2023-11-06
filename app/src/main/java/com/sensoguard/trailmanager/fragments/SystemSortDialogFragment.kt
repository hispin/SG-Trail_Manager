package com.sensoguard.trailmanager.fragments

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.adapters.SystemSortDialogAdapter
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.global.CAMERA_KEY
import com.sensoguard.trailmanager.global.DETECTORS_LIST_KEY_PREF
import com.sensoguard.trailmanager.global.ERROR_RESP
import com.sensoguard.trailmanager.global.SORT_BY_DATETIME_KEY
import com.sensoguard.trailmanager.global.SORT_BY_SYSTEM_KEY
import com.sensoguard.trailmanager.global.SORT_TYPE_KEY
import com.sensoguard.trailmanager.global.convertJsonToSensorList
import com.sensoguard.trailmanager.global.convertToGson
import com.sensoguard.trailmanager.global.getStringFromCalendar
import com.sensoguard.trailmanager.global.getStringInPreference
import com.squareup.timessquare.CalendarPickerView
import java.util.*


class SystemSortDialogFragment : DialogFragment(),
    CalendarPickerView.OnDateSelectedListener {

    private var sortType: Int? = null
    private var myCalendar: CalendarPickerView? = null
    private var myCameras: ArrayList<Camera>? = null
    private var rvSystemCameras: RecyclerView? = null
    private var systemFilterDialogAdapter: SystemSortDialogAdapter? = null
    private var btnApplySort: AppCompatButton? = null
    private var btnCancelSort: AppCompatButton? = null
    private var conTitle: ConstraintLayout? = null
    private var fromCalendar: Calendar? = Calendar.getInstance()
    private var toCalendar: Calendar? = Calendar.getInstance()
    private var tvFromDate: TextView? = null
    private var tvToDate: TextView? = null
    private var btnFromTime: Button? = null
    private var btnToTime: Button? = null
    private var linerCalendarContainer: LinearLayoutCompat? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_sort_system_dialog,
            container,
            false
        )

        initViews(view)


        val bundle = arguments
        sortType = bundle?.getInt(SORT_TYPE_KEY, -1)
        if (sortType == SORT_BY_SYSTEM_KEY) {
            rvSystemCameras?.visibility = View.VISIBLE
            conTitle?.visibility = View.VISIBLE
            myCalendar?.visibility = View.GONE
            linerCalendarContainer?.visibility = View.GONE
            initCamerasAdapter()
        } else if (sortType == SORT_BY_DATETIME_KEY) {
            rvSystemCameras?.visibility = View.GONE
            conTitle?.visibility = View.GONE
            myCalendar?.visibility = View.VISIBLE
            linerCalendarContainer?.visibility = View.VISIBLE
            initDateTime()
            setFromToDateFromCalender()
        }



        return view
    }


    //initialize the date picker
    private fun initDateTime() {
        val nextYear: Calendar = Calendar.getInstance()
        nextYear.add(Calendar.YEAR, 2)

        val prevYear: Calendar = Calendar.getInstance()
        prevYear.add(Calendar.YEAR, -2)

        val today = Date()

        myCalendar?.init(prevYear.time, nextYear.time)?.withSelectedDate(today)
            ?.inMode(CalendarPickerView.SelectionMode.RANGE)

        myCalendar?.setOnDateSelectedListener(this)

        //show the date time in UI
        setFromToDateFromCalender()
    }


    private fun initViews(view: View?) {
        rvSystemCameras = view?.findViewById(R.id.rvSystemCameras)
        btnApplySort = view?.findViewById(R.id.btnApplySort)
        btnApplySort?.setOnClickListener {
            sendResult(true)
        }

        btnCancelSort = view?.findViewById(R.id.btnCancelSort)
        btnCancelSort?.setOnClickListener {
            sendResult(false)
        }

        conTitle = view?.findViewById(R.id.conTitle)
        myCalendar = view?.findViewById(R.id.calendar_view) as CalendarPickerView

        tvFromDate = view.findViewById(R.id.tvFromDate)
        tvToDate = view.findViewById(R.id.tvToDate)
        btnFromTime = view.findViewById(R.id.btnFromTime)
        btnFromTime?.setOnClickListener {
            val hour = fromCalendar?.get(Calendar.HOUR_OF_DAY)
            val minutes = fromCalendar?.get(Calendar.MINUTE)
            if (hour != null && minutes != null) {
                openTimeDialog(hour, minutes, true)
            }
        }
        btnToTime = view.findViewById(R.id.btnToTime)
        btnToTime?.setOnClickListener {
            val hour = toCalendar?.get(Calendar.HOUR_OF_DAY)
            val minutes = toCalendar?.get(Calendar.MINUTE)
            if (hour != null && minutes != null) {
                openTimeDialog(hour, minutes, false)
            }
        }
        linerCalendarContainer = view.findViewById(R.id.linerCalendarContainer)
    }

    //set the current selected date
    private fun setFromToDateFromCalender() {

        val tmp = Calendar.getInstance()

        if (myCalendar?.selectedDates?.size != null && myCalendar?.selectedDates?.size!! > 0) {
            tmp.time = myCalendar?.selectedDates?.first()
            fromCalendar?.set(Calendar.YEAR, tmp.get(Calendar.YEAR))
            fromCalendar?.set(Calendar.MONTH, tmp.get(Calendar.MONTH))
            fromCalendar?.set(Calendar.DAY_OF_MONTH, tmp.get(Calendar.DAY_OF_MONTH))
            //fromCalendar?.time=myCalendar?.selectedDates?.first()
        }
        if (myCalendar?.selectedDates?.size != null && myCalendar?.selectedDates?.size!! > 1) {
            tmp.time = myCalendar?.selectedDates?.last()
            toCalendar?.set(Calendar.YEAR, tmp.get(Calendar.YEAR))
            toCalendar?.set(Calendar.MONTH, tmp.get(Calendar.MONTH))
            toCalendar?.set(Calendar.DAY_OF_MONTH, tmp.get(Calendar.DAY_OF_MONTH))
            //toCalendar?.time=myCalendar?.selectedDates?.last()
        } else if (myCalendar?.selectedDates?.size != null && myCalendar?.selectedDates?.size!! == 1) {
            tmp.time = myCalendar?.selectedDates?.first()
            toCalendar?.set(Calendar.YEAR, tmp.get(Calendar.YEAR))
            toCalendar?.set(Calendar.MONTH, tmp.get(Calendar.MONTH))
            toCalendar?.set(Calendar.DAY_OF_MONTH, tmp.get(Calendar.DAY_OF_MONTH))
            //toCalendar?.time=myCalendar?.selectedDates?.first()
        }

        setFromToTime()
//        fromCalendar?.let {
//            val fromDateStr=activity?.let { it1 -> getStringFromCalendar(it, "dd/MM/yy", it1) }
//            tvFromDate?.text = fromDateStr
//            val fromTimeStr=activity?.let { it1 -> getStringFromCalendar(it, "kk:mm", it1) }
//            btnFromTime?.setText(fromTimeStr)
//        }
//        toCalendar?.let {
//            val toDateStr=activity?.let { it1 -> getStringFromCalendar(it, "dd/MM/yy", it1) }
//            tvToDate?.text = toDateStr
//            val toTimeStr=activity?.let { it1 -> getStringFromCalendar(it, "kk:mm", it1) }
//            btnToTime?.setText(toTimeStr)
//        }
    }

    //set selected time
    private fun setFromToTime() {
        fromCalendar?.let {
            val fromDateStr = activity?.let { it1 -> getStringFromCalendar(it, "dd/MM/yy", it1) }
            tvFromDate?.text = fromDateStr
            val fromTimeStr = activity?.let { it1 -> getStringFromCalendar(it, "kk:mm", it1) }
            btnFromTime?.setText(fromTimeStr)
        }
        toCalendar?.let {
            val toDateStr = activity?.let { it1 -> getStringFromCalendar(it, "dd/MM/yy", it1) }
            tvToDate?.text = toDateStr
            val toTimeStr = activity?.let { it1 -> getStringFromCalendar(it, "kk:mm", it1) }
            btnToTime?.setText(toTimeStr)
        }
    }

    private fun sendResult(isOk: Boolean) {
        if (!isOk) {
            val intent = Intent()
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, intent)
            dismiss()
            return
        }


        if (targetFragment == null) {
            return
        }

        if (sortType == SORT_BY_SYSTEM_KEY) {
            val intent = Intent()
            val cameraStr = myCameras?.let { convertToGson(it) }
            cameraStr?.let {
                val bdl = Bundle()
                bdl.putString(CAMERA_KEY, cameraStr)
                intent.putExtras(bdl)
            }
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        } else if (sortType == SORT_BY_DATETIME_KEY) {

            if (fromCalendar != null && fromCalendar!!.before(toCalendar)) {
                val intent = Intent()
                intent.putExtra("fromCalendar", fromCalendar)
                intent.putExtra("toCalendar", toCalendar)
                targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
                dismiss()
            } else {
                Toast.makeText(
                    activity,
                    resources.getString(R.string.date_older),
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    }

    override fun onStart() {
        super.onStart()
        refreshSystemCamerasFromPref()
    }

    private fun initCamerasAdapter() {

        myCameras = ArrayList()

        if (activity == null) return
        val itemDecorator = DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.divider
            )!!
        )
        rvSystemCameras?.addItemDecoration(itemDecorator)

        systemFilterDialogAdapter = activity?.let { adapter ->
            myCameras?.let { arr ->
                SystemSortDialogAdapter(arr, adapter) { _camera ->
                }
            }
        }
        rvSystemCameras?.adapter = systemFilterDialogAdapter
        val layoutManager = LinearLayoutManager(activity)
        rvSystemCameras?.layoutManager = layoutManager

        systemFilterDialogAdapter?.notifyDataSetChanged()


    }


    private fun refreshSystemCamerasFromPref() {
        myCameras = ArrayList()
        val detectorListStr = getStringInPreference(activity, DETECTORS_LIST_KEY_PREF, ERROR_RESP)

        if (detectorListStr.equals(ERROR_RESP)) {

        } else {

            detectorListStr?.let {
                val temp = convertJsonToSensorList(it)
                temp?.let { tmp ->
                    myCameras = ArrayList(tmp)
                }
            }
        }

        systemFilterDialogAdapter?.setDetects(myCameras)
        systemFilterDialogAdapter?.notifyDataSetChanged()
    }


    override fun onDateSelected(date: Date?) {

        setFromToDateFromCalender()
    }

    override fun onDateUnselected(date: Date?) {


    }

    var tp: TimePickerDialog? = null
    private fun openTimeDialog(mHour: Int, mMinute: Int, isFrom: Boolean) {
        val tp = TimePickerDialog(
            context,
            R.style.DateTimeDialog,
            TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->

                tp?.cancel()

                if (isFrom) {
                    fromCalendar?.set(Calendar.HOUR_OF_DAY, hour)
                    fromCalendar?.set(Calendar.MINUTE, minute)
                } else {
                    toCalendar?.set(Calendar.HOUR_OF_DAY, hour)
                    toCalendar?.set(Calendar.MINUTE, minute)
                }

                setFromToTime()


            },
            mHour,
            mMinute,
            true
        )

        tp.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        tp.show()
    }


}



