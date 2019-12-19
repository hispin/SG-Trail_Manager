package com.sensoguard.hunter.adapters

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
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.Alarm
import com.sensoguard.hunter.global.getStrDateTimeByMilliSeconds
import java.io.File
import java.util.*


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

    fun setDetects(_alarm: ArrayList<Alarm>?) {
        _alarm?.let { alarms = it }
        //TODO how to define with this
    }

    inner class ViewHolder(
        private val _itemView: View,
        private val itemClick: (Alarm, Int) -> Unit
    ) :
        RecyclerView.ViewHolder(_itemView) {
        private var tvName:TextView?=null
        private var tvDate:TextView? = null
        private var tvTime:TextView? = null
        //TODO press twice
        private var tvType: TextView? = null
        private var ivAlarmPic: AppCompatImageView? = null
        private var ivShare: AppCompatImageView? = null
        private var ivIconVideo: AppCompatImageView? = null
        private var pbLoadPhoto: ProgressBar? = null


//        init {
//            itemView.setOnClickListener {
//                itemClick.invoke(alarms[adapterPosition])
//            }
//        }


        fun bindReservation(alarm: Alarm) {
            //tvId = _itemView.findViewById(R.id.tvId)
            tvName = _itemView.findViewById(R.id.tvName)
            tvDate = _itemView.findViewById(R.id.tvDate)
            tvType = _itemView.findViewById(R.id.tvType)
            tvTime = _itemView.findViewById(R.id.tvTime)
            ivAlarmPic = _itemView.findViewById(R.id.ivAlarmPic)
            ivAlarmPic?.setOnClickListener {
                itemClick.invoke(alarm, 1)
                //alarm.imgsPath?.let { it1 -> onAdapterListener.openLargePictureDialog(it1) }
            }
            ivShare = _itemView.findViewById(R.id.ivShare)
            ivShare?.setOnClickListener {
                itemClick.invoke(alarm, 2)
            }
            ivIconVideo = _itemView.findViewById(R.id.ivIconVideo)



            pbLoadPhoto = _itemView.findViewById(R.id.pbLoadPhoto)
            if (alarm.isLoadPhoto) {
                pbLoadPhoto?.visibility = View.VISIBLE
            } else {
                pbLoadPhoto?.visibility = View.GONE
            }


//            if(alarm.isArmed!=null
//                && alarm.isArmed!!
//                && alarm.isLocallyDefined!=null
//                && alarm.isLocallyDefined!!
//            ){
//                tvName?.setTextColor( ContextCompat.getColor(context,R.color.red))
//                tvDate?.setTextColor( ContextCompat.getColor(context,R.color.red))
//                //tvId?.setTextColor( ContextCompat.getColor(context,R.color.red))
//                tvType?.setTextColor( ContextCompat.getColor(context,R.color.red))
//                tvTime?.setTextColor( ContextCompat.getColor(context,R.color.red))
//            }else{
//                tvName?.setTextColor( ContextCompat.getColor(context,R.color.black))
//                tvDate?.setTextColor( ContextCompat.getColor(context,R.color.black))
//                //tvId?.setTextColor( ContextCompat.getColor(context,R.color.black))
//                tvType?.setTextColor( ContextCompat.getColor(context,R.color.black))
//                tvTime?.setTextColor( ContextCompat.getColor(context, R.color.black))
//            }


            if (!isGrid) {
                tvDate?.text = alarm.timeInMillis?.let {
                    getStrDateTimeByMilliSeconds(
                        it,
                        "dd/MM/yyyy",
                        context
                    )
                }
            }else{
                tvDate?.text = alarm.timeInMillis?.let {
                    getStrDateTimeByMilliSeconds(
                        it,
                        "dd/MM/yy",
                        context
                    )
                }
            }
            tvTime?.text= alarm.timeInMillis?.let { getStrDateTimeByMilliSeconds(it,"kk:mm",context) }
            //tvId?.text=alarm.id
            tvType?.text=alarm.type
            tvName?.text = alarm.name

            val imgFile = alarm.imgsPath?.let { File(it) }

            if (imgFile != null && imgFile.exists()) {
                //val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                //ivAlarmPic?.setImageBitmap(myBitmap)

                //imgFile.absolutePath?.let { Picasso.get().load(File(it)).resize(400, 240).centerCrop().into(ivAlarmPic) }
                //Picasso.get().load(File(imgFile.absolutePath)).into(ivAlarmPic)
                ivAlarmPic?.let {
                    Glide.with(context).load(File(imgFile.absolutePath))
                        .listener(object : RequestListener<Drawable> {

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: com.bumptech.glide.load.DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                //if the the image is video then show the video icon
                                if (imgFile.absolutePath.endsWith("mp4")) {
                                    ivIconVideo?.visibility = View.VISIBLE
                                } else {
                                    ivIconVideo?.visibility = View.GONE
                                }

                                return false
                            }

                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Toast.makeText(context, "error loading image", Toast.LENGTH_LONG)
                                    .show()
                                return false
                            }

                        }).into(it)

                }
            }
        }
    }
}