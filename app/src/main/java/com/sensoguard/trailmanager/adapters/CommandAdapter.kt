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
import java.util.*

class CommandAdapter(
    private var commands: ArrayList<Command>,
    val context: Context,
    var itemClick: (Command) -> Unit
) : RecyclerView.Adapter<CommandAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: CommandAdapter.ViewHolder, position: Int) {
        holder.bindReservation((commands[position]))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return this.commands.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): CommandAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_command, parent, false)


        return ViewHolder(view, itemClick)
    }

    fun setCommands(_detectors: ArrayList<Command>?) {
        _detectors?.let { commands = it }
        //TODO how to define with this
    }

    inner class ViewHolder(
        private val _itemView: View,
        private val itemClick: (Command) -> Unit
    ) :
        RecyclerView.ViewHolder(_itemView) {

        private var tvCommandTitle: TextView? = null
        private var ivIcon: AppCompatImageView? = null


        init {
            itemView.setOnClickListener {
                itemClick.invoke(commands[adapterPosition])
            }
        }


        fun bindReservation(command: Command) {
            //tvId = _itemView.findViewById(R.id.tvId)
            tvCommandTitle = _itemView.findViewById(R.id.tvCommandTitle)
            tvCommandTitle?.text = command.commandName
            ivIcon = _itemView.findViewById(R.id.ivIcon)
            if (command.icId != -1) {
                ivIcon?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, command.icId
                    )
                )
            }

        }
    }


}