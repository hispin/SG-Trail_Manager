package com.sensoguard.trailmanager.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.interfaces.OnAdapterListener


//list of cameras in the dialog of the map (select one camera to locate)
class CameraDialogAdapter(
    private var sensors: ArrayList<Camera>,
    val context: Context,
    val onAdapterListener: OnAdapterListener
) : RecyclerView.Adapter<CameraDialogAdapter.ViewHolder>() {


    companion object {
        var selectedSensor: Camera? = null
    }

    init {
        //clear the selected sensor
        selectedSensor = null
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bindReservation((sensors[position]))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return this.sensors.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_sensor_dialog, parent, false)
        return ViewHolder(view)
    }

    //replace all the cameras (the list is reloaded from the storage, so all items may change)
    @SuppressLint("NotifyDataSetChanged")
    fun setDetects(newSensors: ArrayList<Camera>?) {
        newSensors?.let { sensors = it }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val view: View) :
        RecyclerView.ViewHolder(view) {
        private var tvId: TextView? = null
        private var tvName: TextView? = null
        private var ibEditName: ImageButton? = null
        private var togIsSelected: ToggleButton? = null
        private var etName: TextView? = null


        fun bindReservation(sensor: Camera) {
            tvId = view.findViewById(R.id.tvId)
            tvName = view.findViewById(R.id.tvName)
            etName = view.findViewById(R.id.etName)
            ibEditName = view.findViewById(R.id.ibEditName)
            togIsSelected = view.findViewById(R.id.togIsSelected)

            togIsSelected?.isChecked = selectedSensor?.getId().equals(sensor.getId())

            togIsSelected?.setOnCheckedChangeListener { _, isChecked ->
                // to make one selected only: refresh the previous selected item and this item
                val previousIndex = sensors.indexOfFirst { it.getId() == selectedSensor?.getId() }
                selectedSensor = if (isChecked) sensor else null
                if (previousIndex != -1) {
                    notifyItemChanged(previousIndex)
                }
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(bindingAdapterPosition)
                }
            }

            ibEditName?.setOnClickListener {
                tvName?.visibility = View.INVISIBLE
                etName?.visibility = View.VISIBLE
                etName?.requestFocus()
                val manager = context.getSystemService(InputMethodManager::class.java)
                manager?.showSoftInput(etName, 0)
            }

            etName?.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!v.text.isNullOrEmpty()) {
                        sensor.setName(v.text.toString())
                        onAdapterListener.saveNameSensor(sensor)
                    }
                    tvName?.visibility = View.VISIBLE
                    etName?.visibility = View.INVISIBLE
                    val imm = v.context.getSystemService(InputMethodManager::class.java)
                    imm?.hideSoftInputFromWindow(v.windowToken, 0)
                    true
                } else {
                    false
                }
            }

            tvId?.text = sensor.getId()
            tvName?.text = sensor.getName()
            etName?.hint = sensor.getName()

        }
    }
}
