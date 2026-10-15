package com.sensoguard.trailmanager.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.Alarm
import com.sensoguard.trailmanager.global.getStrDateTimeByMilliSeconds
import java.io.File


class AlarmAdapter(
    private var alarms: ArrayList<Alarm>,
    val context: Context,
    val res: Int,
    val isGrid: Boolean,
    var itemClick: (Alarm, Int) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindReservation((alarms[position]))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return this.alarms.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                res, parent, false
            )


        return ViewHolder(view, itemClick)
    }

    //replace all the alarms (the list is reloaded from the storage, so all items may change)
    @SuppressLint("NotifyDataSetChanged")
    fun setDetects(newAlarms: ArrayList<Alarm>?) {
        newAlarms?.let { alarms = it }
        notifyDataSetChanged()
    }

    //the icon of a level (0-100) of battery / signal
    private fun levelIcon(level: Float?, icons: IntArray): Int? {
        return when {
            level == null -> null
            level >= 0 && level < 25 -> icons[0]
            level >= 25 && level < 50 -> icons[1]
            level >= 50 && level < 75 -> icons[2]
            level in 75.0..100.0 -> icons[3]
            else -> null
        }
    }

    inner class ViewHolder(
        private val view: View,
        private val itemClick: (Alarm, Int) -> Unit
    ) :
        RecyclerView.ViewHolder(view) {
        private var tvName: TextView? = null
        private var tvDate: TextView? = null
        private var tvTime: TextView? = null
        private var ivAlarmPic: AppCompatImageView? = null
        private var ivShare: AppCompatImageView? = null
        private var ivIconVideo: AppCompatImageView? = null
        private var pbLoadPhoto: ProgressBar? = null
        private var ivBatteryLevel: AppCompatImageView? = null
        private var ivWifiLevel: AppCompatImageView? = null


        init {
            itemView.setOnLongClickListener {
                toggleReadyToDelete()
                return@setOnLongClickListener false
            }
        }

        //toggle the status of ready to delete
        private fun toggleReadyToDelete() {
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return
            }
            alarms[position].isReadyToDelete = !alarms[position].isReadyToDelete
            notifyItemChanged(position, alarms[position])
        }


        fun bindReservation(myAlarm: Alarm) {
            tvName = view.findViewById(R.id.tvName)
            tvDate = view.findViewById(R.id.tvDate)
            tvTime = view.findViewById(R.id.tvTime)


            ivAlarmPic = view.findViewById(R.id.ivAlarmPic)
            ivAlarmPic?.setOnClickListener {
                itemClick.invoke(myAlarm, 1)
            }

            //the picture has its own click listener, so the long click of the row does not reach it
            ivAlarmPic?.setOnLongClickListener {
                toggleReadyToDelete()
                return@setOnLongClickListener true
            }

            ivShare = view.findViewById(R.id.ivShare)
            ivShare?.setOnClickListener {
                itemClick.invoke(myAlarm, 2)
            }
            ivIconVideo = view.findViewById(R.id.ivIconVideo)

            pbLoadPhoto = view.findViewById(R.id.pbLoadPhoto)
            pbLoadPhoto?.visibility = if (myAlarm.isLoadPhoto) View.VISIBLE else View.GONE

            ivBatteryLevel = view.findViewById(R.id.ivBatteryLevel)
            ivWifiLevel = view.findViewById(R.id.ivWifiLevel)

            val batteryIcons = if (isGrid) {
                intArrayOf(
                    R.drawable.battery_level_1_grid, R.drawable.battery_level_2_grid,
                    R.drawable.battery_level_3_grid, R.drawable.battery_level_4_grid
                )
            } else {
                intArrayOf(
                    R.drawable.battery_level_1_list, R.drawable.battery_level_2_list,
                    R.drawable.battery_level_3_list, R.drawable.battery_level_4_list
                )
            }
            levelIcon(
                myAlarm.batteryVal,
                batteryIcons
            )?.let { ivBatteryLevel?.setImageResource(it) }

            val wifiIcons = if (isGrid) {
                intArrayOf(
                    R.drawable.wifi_level_1_grid, R.drawable.wifi_level_2_grid,
                    R.drawable.wifi_level_3_grid, R.drawable.wifi_level_4_grid
                )
            } else {
                intArrayOf(
                    R.drawable.wifi_level_1_list, R.drawable.wifi_level_2_list,
                    R.drawable.wifi_level_3_list, R.drawable.wifi_level_4_list
                )
            }
            levelIcon(myAlarm.wifiVal, wifiIcons)?.let { ivWifiLevel?.setImageResource(it) }

            val dateFormat = if (isGrid) "dd/MM/yy" else "dd/MM/yyyy"
            tvDate?.text =
                myAlarm.timeInMillis?.let { getStrDateTimeByMilliSeconds(it, dateFormat, context) }
            tvTime?.text =
                myAlarm.timeInMillis?.let { getStrDateTimeByMilliSeconds(it, "kk:mm", context) }
            tvName?.text = myAlarm.name

            //get the image from external storage
            val imgFile = myAlarm.imgsPath?.let { File(it) }

            if (imgFile != null && imgFile.exists()) {

                ivAlarmPic?.let {
                    Glide.with(context).load(File(imgFile.absolutePath))
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                Toast.makeText(
                                    context,
                                    "error loading image" + e?.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                //if the file is a video then show the video icon
                                ivIconVideo?.visibility =
                                    if (imgFile.absolutePath.endsWith("mp4")) View.VISIBLE else View.GONE
                                return false
                            }

                        }).into(it)

                }
            }

            //set selected/unselected
            itemView.alpha = if (myAlarm.isReadyToDelete) 0.5F else 1.0F
        }
    }
}
