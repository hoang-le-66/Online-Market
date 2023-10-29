package com.example.onlinemarket.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.onlinemarket.RvListenerCategory
import com.example.onlinemarket.databinding.RowCategoryBinding
import java.util.Random
import com.example.onlinemarket.models.ModelCategory


class AdapterCategory(
    private val context: Context,
    private val categoryArrayList: ArrayList<ModelCategory>,
    private val rvListenerCategory: RvListenerCategory

) : Adapter<AdapterCategory.HolderCategory>() {
    private lateinit var bindingRowCategory: RowCategoryBinding

    private companion object{
        private const val TAG = "ADAPTER_CATEGORY_TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        bindingRowCategory = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(bindingRowCategory.root)
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val modelCategory = categoryArrayList[position]
        //get data from modelCategory
        val icon = modelCategory.icon
        val category = modelCategory.category

        val random = Random()
        val color = Color.argb(255,random.nextInt(255), random.nextInt(255), random.nextInt(255))

        holder.categoryIconIv.setImageResource(icon)
        holder.categoryTv.text = category
        holder.categoryIconIv.setBackgroundColor(color)

        holder.itemView.setOnClickListener{
            rvListenerCategory.onCategoryClick(modelCategory)
        }

    }

    inner class HolderCategory(itemView: View) : ViewHolder(itemView){
        var categoryIconIv = bindingRowCategory.categoryIconIv
        var categoryTv = bindingRowCategory.categoryTv

    }



}