package com.example.onlinemarket

import android.widget.Filter
import com.example.onlinemarket.adapters.AdapterHistoryChats
import com.example.onlinemarket.models.ModelHistoryChats

class FilterChats : Filter {

    private val adapterChats: AdapterHistoryChats
    private val filterList: ArrayList<ModelHistoryChats>
    /*
       *Filter Chats Constructor
       *
       * @param adapter: AdapterHistoryChats instance to be passed when this cons is created
       * @param filterList: HistoryChats ArrayList to be pass when this cons is created
       * */
    constructor(
        adapterChats: AdapterHistoryChats,
        filterList: ArrayList<ModelHistoryChats>
    ) : super() {
        this.adapterChats = adapterChats
        this.filterList = filterList
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()

        if (!constraint.isNullOrEmpty()){
            constraint = constraint.toString().uppercase()
            //hold the filtered list of Ads based on user searched query
            val filteredModels = ArrayList<ModelHistoryChats>()
            for (i in filterList.indices){
                //filter based on Receipt User Name
                if (filterList[i].name.uppercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }

            }

            //Typing matched item
            results.count = filteredModels.size
            results.values = filteredModels

        }else{
            //Querying null or empty return full list
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        adapterChats.chatsHistoryArrayList = results.values as ArrayList<ModelHistoryChats>
        adapterChats.notifyDataSetChanged()
    }
}