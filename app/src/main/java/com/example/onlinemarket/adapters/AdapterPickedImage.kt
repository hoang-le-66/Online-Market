package com.example.onlinemarket.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.util.Util
import com.example.onlinemarket.R
import com.example.onlinemarket.Utils
import com.example.onlinemarket.databinding.RowImagesPickedBinding
import com.example.onlinemarket.models.ModelPickedImage
import com.google.firebase.database.FirebaseDatabase

class AdapterPickedImage(
    private val context: Context,
    private val imagesPickedArrayList: ArrayList<ModelPickedImage>,
    private val adId: String

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
        //check if img is from firebase storage or device storage
        if (model.fromInternet){

            try {
                val imageUrl = model.imageUrl
                Log.d(TAG, "onBindViewHolder: imageUrl: $imageUrl")

                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.imageIv)
            }catch (e: Exception){
                Log.e(TAG, "onBindViewHolder: ", e)
            }

        }else{
            //Image is picked from Gallery/Camera. Get image Uri of the image to set in imageIv
            try {
                //get imageUri
                val imageUri = model.imageUri
                Log.d(TAG, "onBindViewHolder: imageUri: $imageUri")

                Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.imageIv)
            }catch (e: Exception){
                Log.e(TAG, "onBindViewHolder: ", e)

            }
        }


        //if image is picked from device storage then just remove from list
        //if from firebase storage then first delete from firebase storage
        holder.closeBtn.setOnClickListener {
            //check if img is from Device storage or Firebase
            if (model.fromInternet){
                deleteImageFirebase(model, holder, position)

            }else{
                imagesPickedArrayList.remove(model)
                notifyDataSetChanged()
            }


        }

    }

    private fun deleteImageFirebase(
        model: ModelPickedImage,
        holder: AdapterPickedImage.HolderPickedImage,
        position: Int
    ) {
        val imageId = model.id

        Log.d(TAG, "deleteImageFirebase: adId: $adId ")
        Log.d(TAG, "deleteImageFirebase: imageId: $imageId")

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId).child("Images").child(imageId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "deleteImageFirebase: Image $imageId deleted")
                Utils.toast(context, "Image deleted")
                //remove from imagesPickedArrayList
                try {
                    imagesPickedArrayList.remove(model)
                    notifyItemRemoved(position)
                }catch (e: Exception){
                    Log.d(TAG, "deleteImageFirebase: ", e)
                }

            }
            .addOnFailureListener {e ->
                Log.e(TAG, "deleteImageFirebase: ", e)
                Utils.toast(context, "Failed to delete img due to ${e.message}")
            }
    }

    inner class HolderPickedImage(itemView: View) : ViewHolder(itemView){
        var imageIv = bindingRowImgPick.imageIv
        var closeBtn = bindingRowImgPick.closeBtn

    }


}