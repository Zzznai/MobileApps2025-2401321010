package com.naim.plantcare.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.naim.plantcare.R
import com.naim.plantcare.model.Plant
import com.naim.plantcare.ui.PlantItem
import com.naim.plantcare.ui.WateringStatusText

class PlantAdapter(
    private val onPlantClick: (Plant) -> Unit
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {
    private var items: List<PlantItem> = emptyList()

    class PlantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitial: TextView = view.findViewById(R.id.tvInitial)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvSpecies: TextView = view.findViewById(R.id.tvSpecies)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    fun submitList(newItems: List<PlantItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val item = items[position]
        holder.tvInitial.text = item.plant.name.take(1)
        holder.tvName.text = item.plant.name
        holder.tvSpecies.text = item.plant.species
        WateringStatusText.show(holder.tvStatus, item.status)
        holder.itemView.setOnClickListener { onPlantClick(item.plant) }
    }

    override fun getItemCount(): Int = items.size
}
