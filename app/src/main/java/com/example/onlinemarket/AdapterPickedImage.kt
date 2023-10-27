package com.example.onlinemarket

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.onlinemarket.databinding.RowImagesPickedBinding

class AdapterPickedImage(
    private val context: Context,
    private val imagesPickedArrayList: ArrayList<ModelPickedImage>
    ): Adapter<AdapterPickedImage.HolderPickedImage>() {

    private lateinit var bindingRowImgPick: RowImagesPickedBinding

    private companion object{
        private const val TAG = "IMAGES_TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPickedImage {
        bindingRowImgPick = RowImagesPickedBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPickedImage(bindingRowImgPick.root)


    }

    override fun getItemCount(): Int {
        return imagesPickedArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPickedImage, position: Int) {
        val model = imagesPickedArrayList[position]
        //get uri of the image to set in imageIv
        val imageUri = model.imageUri
        Log.d(TAG, "onBindViewHolder: imageUri $imageUri")

        try {
            Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.ic_image_gray)
                .into(holder.imageIv)
        }catch (e: Exception){
            Log.e(TAG, "onBindViewHolder: ", e)

        }

        holder.closeBtn.setOnClickListener {
            imagesPickedArrayList.remove(model)
            notifyDataSetChanged()
        }

    }

    inner class HolderPickedImage(itemView: View) : ViewHolder(itemView){
        var imageIv = bindingRowImgPick.imageIv
        var closeBtn = bindingRowImgPick.closeBtn

    }


}