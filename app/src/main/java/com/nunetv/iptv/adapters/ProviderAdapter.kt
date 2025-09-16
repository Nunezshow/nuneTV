package com.nunetv.iptv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nunetv.iptv.R
import com.nunetv.iptv.model.ProviderCredentials

class ProviderAdapter(
    private val onProviderSelected: (ProviderCredentials) -> Unit
) : RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder>() {

    private var providers: List<ProviderCredentials> = emptyList()
    private var selectedName: String? = null
    private var activeName: String? = null

    fun submitList(items: List<ProviderCredentials>, activeProvider: String?) {
        providers = items
        activeName = activeProvider
        if (selectedName != null && providers.none { it.name == selectedName }) {
            selectedName = null
        }
        notifyDataSetChanged()
    }

    fun select(provider: ProviderCredentials) {
        selectedName = provider.name
        notifyDataSetChanged()
    }

    fun getSelected(): ProviderCredentials? {
        return providers.firstOrNull { it.name == selectedName } ?: providers.firstOrNull()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_provider, parent, false)
        return ProviderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val provider = providers[position]
        holder.bind(provider, provider.name == selectedName, provider.name == activeName)
        holder.itemView.setOnClickListener {
            selectedName = provider.name
            notifyDataSetChanged()
            onProviderSelected(provider)
        }
        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                selectedName = provider.name
                notifyDataSetChanged()
                onProviderSelected(provider)
            }
        }
    }

    override fun getItemCount(): Int = providers.size

    class ProviderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.provider_name)
        private val detailsView: TextView = itemView.findViewById(R.id.provider_details)
        private val statusView: TextView = itemView.findViewById(R.id.provider_status)

        fun bind(credentials: ProviderCredentials, selected: Boolean, active: Boolean) {
            nameView.text = credentials.name
            detailsView.text = "${credentials.username} • ${credentials.portalUrl}"
            statusView.visibility = if (active) View.VISIBLE else View.GONE
            itemView.isSelected = selected
        }
    }
}
