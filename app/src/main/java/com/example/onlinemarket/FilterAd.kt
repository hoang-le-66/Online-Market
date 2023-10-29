package com.example.onlinemarket

import android.widget.Filter
import com.example.onlinemarket.adapters.AdapterAd
import com.example.onlinemarket.models.ModelAd
import java.util.Locale


class FilterAd(
    private val adapter: AdapterAd,
    private val filterList: ArrayList<ModelAd>
) : Filter(){
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if (!constraint.isNullOrEmpty()){
            constraint = constraint.toString().uppercase(Locale.getDefault())

            val filteredModels = ArrayList<ModelAd>()
            for (i in filterList.indices){

                if (filterList[i].brand.uppercase(Locale.getDefault()).contains(constraint) ||
                    filterList[i].category.uppercase(Locale.getDefault()).contains(constraint) ||
                    filterList[i].condition.uppercase(Locale.getDefault()).contains(constraint) ||
                    filterList[i].title.uppercase(Locale.getDefault()).contains(constraint)
                ){
                    filteredModels.add(filterList[i])
                }
            }

            results.count = filterList.size
            results.values = filteredModels
        }else{
            results.count = filterList.size
            results.values = filterList
        }
        return results

    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapter.adArrayList = results.values as ArrayList<ModelAd>
        adapter.notifyDataSetChanged()
    }


}