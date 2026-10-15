package com.sensoguard.trailmanager.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.Command

//list of the commands of a camera
class CommandAdapter(
    private val commands: ArrayList<Command>,
    val context: Context,
    var itemClick: (Command) -> Unit
) : RecyclerView.Adapter<CommandAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindReservation((commands[position]))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return this.commands.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_command, parent, false)
        return ViewHolder(view, itemClick)
    }

    inner class ViewHolder(
        private val view: View,
        private val itemClick: (Command) -> Unit
    ) :
        RecyclerView.ViewHolder(view) {

        private var tvCommandTitle: TextView? = null
        private var ivIcon: AppCompatImageView? = null


        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClick.invoke(commands[position])
                }
            }
        }


        fun bindReservation(command: Command) {
            tvCommandTitle = view.findViewById(R.id.tvCommandTitle)
            tvCommandTitle?.text = command.commandName
            ivIcon = view.findViewById(R.id.ivIcon)
            if (command.icId != -1) {
                ivIcon?.setImageDrawable(ContextCompat.getDrawable(context, command.icId))
            }

        }
    }


}
