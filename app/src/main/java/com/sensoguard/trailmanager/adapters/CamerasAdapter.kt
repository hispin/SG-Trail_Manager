package com.sensoguard.trailmanager.adapters

//import android.support.v7.widget.RecyclerView

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.Camera
import com.sensoguard.trailmanager.global.COMMANDS_ACTION_TYPE
import com.sensoguard.trailmanager.global.DELETE_ACTION_TYPE
import com.sensoguard.trailmanager.global.EDIT_ACTION_TYPE
import com.sensoguard.trailmanager.interfaces.OnAdapterListener


class CamerasAdapter(
    private var cameras: ArrayList<Camera>,
    val context: Context,
    val onAdapterListener: OnAdapterListener,
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

    fun setDetects(_detectors: ArrayList<Camera>?) {
        _detectors?.let { cameras = it }
        //TODO how to define with this
    }

    inner class ViewHolder(
        private val _itemView: View,
        private val itemClick: (Camera, Int) -> Unit
    ) :
        RecyclerView.ViewHolder(_itemView) {
        //private var tvId: TextView?=null
        private var tvName: TextView?=null
        private var tvPhoneNum: TextView? = null
        private var ibEditCamera: AppCompatImageButton? = null
        private var ibDelete: ImageButton? = null
        private var ibSendCommand: AppCompatImageButton? = null
        private var swipe: SwipeLayout? = null
        private var rlDelete: RelativeLayout? = null
        //private var togIsActive: ToggleButton?=null
        //private var ibEditName:ImageButton?=null
        //private var tvIsLocate:TextView?=null

        //TODO press twice
        var etName: TextView?=null


//        init {
//            itemView.setOnClickListener {
//                itemClick.invoke(sensors[adapterPosition])
//            }
//        }

        inner class SwipeDetector : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                TODO("Not yet implemented")
            }

        }


        fun bindReservation(sensor: Camera) {
            //tvId = _itemView.findViewById(R.id.tvId)
           tvName = _itemView.findViewById(R.id.tvName)
            tvPhoneNum = _itemView.findViewById(R.id.tvPhoneNum)
            ibEditCamera = _itemView.findViewById(R.id.ibEditCamera)
            ibDelete = _itemView.findViewById(R.id.ibDelete)
            ibSendCommand = _itemView.findViewById(R.id.ibSendCommand)
            swipe = _itemView.findViewById(R.id.swipe)
            rlDelete = _itemView.findViewById(R.id.rlDelete)

//            swipe?.setOnTouchListener(object: OnSwipeTouchListener(context) {
//                override fun onSwipeLeft() {
//                    super.onSwipeLeft()
//                    swipe?.setBackgroundColor(ContextCompat.getColor(context, R.color.gray4))
//                    //rlDelete?.setBackgroundColor(ContextCompat.getColor(context, R.color.gray4))
//                }
//                override fun onSwipeRight() {
//                    super.onSwipeRight()
//                    swipe?.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
//                    //rlDelete?.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
//                }
//            }
//            )


            ibEditCamera?.setOnClickListener {
                itemClick.invoke(cameras[adapterPosition], EDIT_ACTION_TYPE)
                return@setOnClickListener
            }

            ibDelete?.setOnClickListener {
                itemClick.invoke(cameras[adapterPosition], DELETE_ACTION_TYPE)
                return@setOnClickListener
            }

            ibSendCommand?.setOnClickListener {
                itemClick.invoke(cameras[adapterPosition], COMMANDS_ACTION_TYPE)
                return@setOnClickListener
            }






            //tvId?.text = sensor.getId()
               tvName?.text = sensor.getName()
            tvPhoneNum?.text = sensor.phoneNum
            //etName?.hint= sensor.getName()

        }
    }
}