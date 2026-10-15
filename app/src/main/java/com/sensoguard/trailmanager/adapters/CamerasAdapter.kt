package com.sensoguard.trailmanager.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.global.COMMANDS_ACTION_TYPE
import com.sensoguard.trailmanager.global.DELETE_ACTION_TYPE
import com.sensoguard.trailmanager.global.EDIT_ACTION_TYPE


//list of the cameras (main screen)
class CamerasAdapter(
    private var cameras: ArrayList<Camera>,
    var itemClick: (Camera, Int) -> Unit
) : RecyclerView.Adapter<CamerasAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindReservation((cameras[position]))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return this.cameras.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_camera, parent, false)
        return ViewHolder(view, itemClick)
    }

    //replace all the cameras (the list is reloaded from the storage, so all items may change)
    @SuppressLint("NotifyDataSetChanged")
    fun setDetects(newCameras: ArrayList<Camera>?) {
        newCameras?.let { cameras = it }
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val view: View,
        private val itemClick: (Camera, Int) -> Unit
    ) :
        RecyclerView.ViewHolder(view) {
        private var tvName: TextView? = null
        private var tvPhoneNum: TextView? = null
        private var ibEditCamera: AppCompatImageButton? = null
        private var ibDelete: ImageButton? = null
        private var ibSendCommand: AppCompatImageButton? = null

        //invoke the action on the camera of this row
        private fun invokeAction(actionType: Int) {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                itemClick.invoke(cameras[position], actionType)
            }
        }

        fun bindReservation(sensor: Camera) {
            tvName = view.findViewById(R.id.tvName)
            tvPhoneNum = view.findViewById(R.id.tvPhoneNum)
            ibEditCamera = view.findViewById(R.id.ibEditCamera)
            ibDelete = view.findViewById(R.id.ibDelete)
            ibSendCommand = view.findViewById(R.id.ibSendCommand)

            ibEditCamera?.setOnClickListener { invokeAction(EDIT_ACTION_TYPE) }
            ibDelete?.setOnClickListener { invokeAction(DELETE_ACTION_TYPE) }
            ibSendCommand?.setOnClickListener { invokeAction(COMMANDS_ACTION_TYPE) }

            tvName?.text = sensor.getName()
            tvPhoneNum?.text = sensor.phoneNum
        }
    }
}
