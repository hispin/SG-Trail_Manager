package com.sensoguard.hunter.fragments

//import android.support.v4.app.Fragment

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
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
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HistoryWarningFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HistoryWarningFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HistoryWarningFragment : Fragment(), OnAdapterListener {
    override fun saveCamera(detector: Camera) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveNameSensor(detector: Camera) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var alarms:ArrayList<Alarm>?=null
    private var rvAlarm:RecyclerView?=null
    private var alarmAdapter: AlarmAdapter?=null
    private var btnCsv: Button?=null
    private var viewBetweenContainer: View? = null

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

        alarms = ArrayList()
        //alarms?.add(Alarm("ID", "NAME", "TYPE", "TIME", false, -1))
//        val itemDecorator= DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL)
//        itemDecorator.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.divider_alarm_log)!!)
//        rvAlarm?.addItemDecoration(itemDecorator)
//        rvAlarm?.addItemDecoration(
//            DividerItemDecoration(
//                context,
//                DividerItemDecoration.HORIZONTAL
//            )
//        )

        alarmAdapter = activity?.let { adapter ->
            alarms?.let { arr ->
                AlarmAdapter(
                    arr, adapter,
                    R.layout.alarm_item_grid, true, HistoryWarningFragment@ this
                ) { _ ->

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

        alarms= ArrayList()
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
            alarms?.let { arr ->
                AlarmAdapter(
                    arr,
                    adapter,
                    R.layout.alarm_item,
                    false,
                    HistoryWarningFragment@ this
                ) { _ ->

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

//        btnCsv=view.findViewById(R.id.btnCsv)
//
//        btnCsv?.setOnClickListener{
//            val alarms=populateAlarmsFromLocally()
//            //val csvFile=CsvFile()
//            HistoryWarningFragment@this.context?.let {
//                    it1 ->
//
//                    val alarmsStr=alarmsListToCsvFile(alarms, it1)
//                    if(writeCsvFile(alarmsStr)){
//                        activity?.let { it2 -> shareCsv(it2) }
//                        //Toast.makeText(context,"success",Toast.LENGTH_SHORT).show()
//                    }else{
//                        Toast.makeText(context,"failed",Toast.LENGTH_SHORT).show()
//                    }
//
//
//            }
//
//        }

        // Inflate the layout for this fragment
        return view
    }

    private fun setFilter() {
        val filter = IntentFilter(CREATE_ALARM_KEY)
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
        alarms=ArrayList()
        //create title
//        alarms?.add(Alarm(
//            resources.getString(R.string.id_title),
//            resources.getString(R.string.name_title),
//            resources.getString(R.string.type_title),
//            resources.getString(R.string.time_title),
//            false,
//            -1
//        ))
        val _alarms=populateAlarmsFromLocally()

        //show only alarm log from email
        val iteratorList = _alarms?.listIterator()
        while (iteratorList != null && iteratorList.hasNext()) {
            val item = iteratorList.next()
            if (item.isCameFromEmail) {
                alarms?.add(item)
            }
        }

        alarms?.let { myAlarms ->
            alarms = ArrayList(myAlarms.sortedWith(compareByDescending { it.timeInMillis }))
            //myAlarms?.let { alarms?.addAll(it) }

            alarmAdapter?.setDetects(alarms)
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



    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        //listener?.onFragmentInteraction(uri)
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
            HistoryWarningFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, inn: Intent) {
            //accept currentAlarm
            if (inn.action == CREATE_ALARM_KEY) {

                Log.d("testLogAlarm", "accept alarm")

                refreshAlarmsFromPref()


            }
        }
    }



}
