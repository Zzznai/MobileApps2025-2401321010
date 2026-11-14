package com.naim.plantcare.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naim.plantcare.R
import com.naim.plantcare.data.entity.Plant
import com.naim.plantcare.ui.PlantItem
import com.naim.plantcare.ui.WateringStatusText

class PlantAdapter(
    private val onPlantClick: (Plant) -> Unit
) : ListAdapter<PlantItem, PlantAdapter.PlantViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(getItem(position), onPlantClick)
    }

    class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvInitial: TextView = itemView.findViewById(R.id.tvInitial)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvSpecies: TextView = itemView.findViewById(R.id.tvSpecies)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(item: PlantItem, onPlantClick: (Plant) -> Unit) {
            tvInitial.text = item.plant.name.take(1)
            tvName.text = item.plant.name
            tvCategory.text = item.category?.name
                ?: itemView.context.getString(R.string.no_category)
            tvSpecies.text = item.plant.species
            WateringStatusText.show(tvStatus, item.status)
            itemView.setOnClickListener { onPlantClick(item.plant) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PlantItem>() {
            override fun areItemsTheSame(oldItem: PlantItem, newItem: PlantItem): Boolean =
                oldItem.plant.id == newItem.plant.id

            override fun areContentsTheSame(oldItem: PlantItem, newItem: PlantItem): Boolean =
                oldItem == newItem
        }
    }
}
