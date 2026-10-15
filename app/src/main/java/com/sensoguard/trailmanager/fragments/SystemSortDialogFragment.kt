package com.sensoguard.trailmanager.fragments

import android.app.TimePickerDialog
import android.graphics.Color
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
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.adapters.SystemSortDialogAdapter
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.global.CAMERA_KEY
import com.sensoguard.trailmanager.global.RESULT_IS_OK_KEY
import com.sensoguard.trailmanager.global.SORT_BY_DATETIME_KEY
import com.sensoguard.trailmanager.global.SORT_BY_SYSTEM_KEY
import com.sensoguard.trailmanager.global.SORT_BY_SYSTEM_RESULT_KEY
import com.sensoguard.trailmanager.global.SORT_FROM_TIME_KEY
import com.sensoguard.trailmanager.global.SORT_PICK_DATE_TIME_RESULT_KEY
import com.sensoguard.trailmanager.global.SORT_TO_TIME_KEY
import com.sensoguard.trailmanager.global.SORT_TYPE_KEY
import com.sensoguard.trailmanager.global.convertToGson
import com.sensoguard.trailmanager.global.getCamerasFromLocally
import com.sensoguard.trailmanager.global.getStringFromCalendar
import com.squareup.timessquare.CalendarPickerView
import java.util.Calendar
import java.util.Date


//filter of the alarm log: by cameras or by date range
//the result is returned to AlarmLogFragment with the keys SORT_BY_SYSTEM_RESULT_KEY / SORT_PICK_DATE_TIME_RESULT_KEY
class SystemSortDialogFragment : DialogFragment(),
    CalendarPickerView.OnDateSelectedListener {

    private var sortType: Int? = null
    private var myCalendar: CalendarPickerView? = null
    private var myCameras: ArrayList<Camera> = ArrayList()
    private var rvSystemCameras: RecyclerView? = null
    private var systemFilterDialogAdapter: SystemSortDialogAdapter? = null
    private var btnApplySort: AppCompatButton? = null
    private var btnCancelSort: AppCompatButton? = null
    private var conTitle: ConstraintLayout? = null
    private val fromCalendar: Calendar = Calendar.getInstance()
    private val toCalendar: Calendar = Calendar.getInstance()
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

        sortType = arguments?.getInt(SORT_TYPE_KEY, -1)
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
        }

        return view
    }


    //initialize the date picker
    private fun initDateTime() {
        val nextYear: Calendar = Calendar.getInstance()
        nextYear.add(Calendar.YEAR, 2)

        val prevYear: Calendar = Calendar.getInstance()
        prevYear.add(Calendar.YEAR, -2)

        myCalendar?.init(prevYear.time, nextYear.time)?.withSelectedDate(Date())
            ?.inMode(CalendarPickerView.SelectionMode.RANGE)

        myCalendar?.setOnDateSelectedListener(this)

        //show the date time in UI
        setFromToDateFromCalender()
    }


    private fun initViews(view: View) {
        rvSystemCameras = view.findViewById(R.id.rvSystemCameras)
        btnApplySort = view.findViewById(R.id.btnApplySort)
        btnApplySort?.setOnClickListener {
            sendResult(true)
        }

        btnCancelSort = view.findViewById(R.id.btnCancelSort)
        btnCancelSort?.setOnClickListener {
            sendResult(false)
        }

        conTitle = view.findViewById(R.id.conTitle)
        myCalendar = view.findViewById(R.id.calendar_view)

        tvFromDate = view.findViewById(R.id.tvFromDate)
        tvToDate = view.findViewById(R.id.tvToDate)
        btnFromTime = view.findViewById(R.id.btnFromTime)
        btnFromTime?.setOnClickListener {
            openTimeDialog(
                fromCalendar.get(Calendar.HOUR_OF_DAY),
                fromCalendar.get(Calendar.MINUTE),
                true
            )
        }
        btnToTime = view.findViewById(R.id.btnToTime)
        btnToTime?.setOnClickListener {
            openTimeDialog(
                toCalendar.get(Calendar.HOUR_OF_DAY),
                toCalendar.get(Calendar.MINUTE),
                false
            )
        }
        linerCalendarContainer = view.findViewById(R.id.linerCalendarContainer)
    }

    //copy the date (without the time) to the calendar
    private fun setDate(calendar: Calendar, date: Date) {
        val tmp = Calendar.getInstance()
        tmp.time = date
        calendar.set(Calendar.YEAR, tmp.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, tmp.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, tmp.get(Calendar.DAY_OF_MONTH))
    }

    //set the current selected date (the range is from the first to the last selected date)
    private fun setFromToDateFromCalender() {
        val selectedDates = myCalendar?.selectedDates
        if (!selectedDates.isNullOrEmpty()) {
            setDate(fromCalendar, selectedDates.first())
            setDate(toCalendar, selectedDates.last())
        }
        setFromToTime()
    }

    //set selected time
    private fun setFromToTime() {
        val context = activity ?: return
        tvFromDate?.text = getStringFromCalendar(fromCalendar, "dd/MM/yy", context)
        btnFromTime?.text = getStringFromCalendar(fromCalendar, "kk:mm", context)
        tvToDate?.text = getStringFromCalendar(toCalendar, "dd/MM/yy", context)
        btnToTime?.text = getStringFromCalendar(toCalendar, "kk:mm", context)
    }

    //the key of the result according to the type of the sort
    private fun getResultKey(): String =
        if (sortType == SORT_BY_SYSTEM_KEY) SORT_BY_SYSTEM_RESULT_KEY else SORT_PICK_DATE_TIME_RESULT_KEY

    private fun sendResult(isOk: Boolean) {
        if (!isOk) {
            parentFragmentManager.setFragmentResult(
                getResultKey(),
                Bundle().apply { putBoolean(RESULT_IS_OK_KEY, false) })
            dismiss()
            return
        }

        if (sortType == SORT_BY_SYSTEM_KEY) {
            parentFragmentManager.setFragmentResult(
                SORT_BY_SYSTEM_RESULT_KEY,
                Bundle().apply {
                    putBoolean(RESULT_IS_OK_KEY, true)
                    putString(CAMERA_KEY, convertToGson(myCameras))
                }
            )
            dismiss()
        } else if (sortType == SORT_BY_DATETIME_KEY) {

            if (fromCalendar.before(toCalendar)) {
                parentFragmentManager.setFragmentResult(
                    SORT_PICK_DATE_TIME_RESULT_KEY,
                    Bundle().apply {
                        putBoolean(RESULT_IS_OK_KEY, true)
                        putLong(SORT_FROM_TIME_KEY, fromCalendar.timeInMillis)
                        putLong(SORT_TO_TIME_KEY, toCalendar.timeInMillis)
                    }
                )
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
        val context = activity ?: return

        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(context, R.drawable.divider)
            ?.let { itemDecorator.setDrawable(it) }
        rvSystemCameras?.addItemDecoration(itemDecorator)

        systemFilterDialogAdapter = SystemSortDialogAdapter(myCameras)
        rvSystemCameras?.adapter = systemFilterDialogAdapter
        rvSystemCameras?.layoutManager = LinearLayoutManager(context)
    }


    private fun refreshSystemCamerasFromPref() {
        val context = activity ?: return
        myCameras = getCamerasFromLocally(context)?.let { ArrayList(it) } ?: ArrayList()
        systemFilterDialogAdapter?.setDetects(myCameras)
    }


    override fun onDateSelected(date: Date?) {
        setFromToDateFromCalender()
    }

    override fun onDateUnselected(date: Date?) {
        //the range is updated when a date is selected
    }

    private fun openTimeDialog(mHour: Int, mMinute: Int, isFrom: Boolean) {
        val tp = TimePickerDialog(
            context,
            R.style.DateTimeDialog,
            { _, hour, minute ->
                val calendar = if (isFrom) fromCalendar else toCalendar
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                setFromToTime()
            },
            mHour,
            mMinute,
            true
        )

        tp.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        tp.show()
    }


}
