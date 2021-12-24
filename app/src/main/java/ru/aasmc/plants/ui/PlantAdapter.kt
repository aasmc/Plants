package ru.aasmc.plants.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.aasmc.plants.databinding.ListItemPlantBinding
import ru.aasmc.plants.model.Plant

class PlantAdapter : ListAdapter<Plant, RecyclerView.ViewHolder>(PLANT_COMPARATOR) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val plant = getItem(position)
        (holder as PlantViewHolder).bind(plant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PlantViewHolder(
            ListItemPlantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    class PlantViewHolder(
        private val binding: ListItemPlantBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Plant) {
            binding.apply {
                plant = item
                executePendingBindings()
            }
        }
    }

    companion object {
        val PLANT_COMPARATOR = object : DiffUtil.ItemCallback<Plant>() {
            override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean {
                return oldItem.plantId == newItem.plantId
            }
        }
    }
}