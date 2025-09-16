package com.nunetv.iptv.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.Presenter
import com.nunetv.iptv.R
import com.nunetv.iptv.model.EpgProgram
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EpgProgramPresenter : Presenter() {

    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_epg_program, parent, false)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val program = item as? EpgProgram ?: return
        val titleView = viewHolder.view.findViewById<TextView>(R.id.program_title)
        val timeView = viewHolder.view.findViewById<TextView>(R.id.program_time)
        val descriptionView = viewHolder.view.findViewById<TextView>(R.id.program_description)
        titleView.text = program.title.ifBlank { viewHolder.view.context.getString(R.string.no_channels) }
        val start = Date(program.startTime)
        val end = Date(program.endTime)
        timeView.text = "${timeFormatter.format(start)} - ${timeFormatter.format(end)}"
        descriptionView.text = program.description.orEmpty()
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        // No-op
    }
}
