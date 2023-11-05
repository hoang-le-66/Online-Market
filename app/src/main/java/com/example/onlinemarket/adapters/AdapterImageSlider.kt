package com.example.onlinemarket.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.example.onlinemarket.R
import com.example.onlinemarket.databinding.RowImageSliderBinding
import com.example.onlinemarket.models.ModelImageSlider
import com.google.android.material.imageview.ShapeableImageView

class AdapterImageSlider: Adapter<AdapterImageSlider.HolderImageSlider> {

    private lateinit var bindingRowImageSlider: RowImageSliderBinding

    private companion object{
        private const val TAG = "IMAGE_SLIDE_TAG"
    }
    //Context of act/fragment from where instance of AdapterAd class is created
    private var context: Context
    //
    private var imageArrayList: ArrayList<ModelImageSlider>

    constructor(context: Context, imageArrayList: ArrayList<ModelImageSlider>) {
        this.context = context
        this.imageArrayList = imageArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImageSlider {
        bindingRowImageSlider = RowImageSliderBinding.inflate(LayoutInflater.from(context), parent, false)

        bindingRowImageSlider.root

        return HolderImageSlider(bindingRowImageSlider.root)
    }

    override fun onBindViewHolder(holder: HolderImageSlider, position: Int) {
        val modelImageSlider = imageArrayList[position]

        val imageUrl = modelImageSlider.imageUrl
        val imageCount = "${position+1}/${imageArrayList.size}"

        holder.imageCountTv.text = imageCount
        try {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_gray)
                .into(holder.imageIv)
        }catch (e: Exception){
            Log.e(TAG, "onBindViewHolder: ", e)
        }

        holder.itemView.setOnClickListener {

        }

    }

    override fun getItemCount(): Int {
        return imageArrayList.size
    }


    inner class HolderImageSlider(itemView: View) : RecyclerView.ViewHolder(itemView){
        //init UI Views of the row_ad.xml
        val imageIv: ShapeableImageView = bindingRowImageSlider.imageIv
        var imageCountTv: TextView = bindingRowImageSlider.imageCountTv

    }


}