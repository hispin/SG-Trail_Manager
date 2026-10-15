package com.sensoguard.trailmanager.adapters


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.Camera


//list of cameras to filter the alarms by them
class SystemSortDialogAdapter(
    private var systems: ArrayList<Camera>
) : RecyclerView.Adapter<SystemSortDialogAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bindReservation((systems[position]))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return this.systems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_system_filter_dialog, parent, false)
        return ViewHolder(view)
    }

    //replace all the cameras (the list is reloaded from the storage, so all items may change)
    @SuppressLint("NotifyDataSetChanged")
    fun setDetects(newSystems: ArrayList<Camera>?) {
        newSystems?.let { systems = it }
        notifyDataSetChanged()
    }

    class ViewHolder(private val view: View) :
        RecyclerView.ViewHolder(view) {
        private var tvId: TextView? = null
        private var tvName: TextView? = null
        private var togIsSelected: ToggleButton? = null


        fun bindReservation(myCamera: Camera) {
            tvId = view.findViewById(R.id.tvId)
            tvName = view.findViewById(R.id.tvName)

            togIsSelected = view.findViewById(R.id.togIsSelected)

            togIsSelected?.setOnCheckedChangeListener { _, isChecked ->
                myCamera.isSorted = isChecked
            }

            tvId?.text = myCamera.getId()
            tvName?.text = myCamera.getName()

        }
    }
}
