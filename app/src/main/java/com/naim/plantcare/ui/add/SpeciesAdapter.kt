package com.naim.plantcare.ui.add

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class SpeciesAdapter(context: Context, private val allLabels: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, ArrayList(allLabels)) {

    private val containsFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val matches = AddPlantViewModel.filterLabels(allLabels, constraint)
            return FilterResults().apply {
                values = matches
                count = matches.size
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            clear()
            addAll(results?.values as? List<String> ?: allLabels)
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter = containsFilter
}
