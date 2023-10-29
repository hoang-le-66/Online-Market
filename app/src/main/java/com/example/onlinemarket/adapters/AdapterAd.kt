package com.example.onlinemarket.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlinemarket.FilterAd
import com.example.onlinemarket.R
import com.example.onlinemarket.Utils
import com.example.onlinemarket.databinding.RowAdBinding
import com.example.onlinemarket.models.ModelAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterAd : RecyclerView.Adapter<AdapterAd.HolderAd>, Filterable{

    private lateinit var bindingAdapterAd: RowAdBinding

    private companion object{
        private const val TAG = "ADAPTER_AD_TAG"
    }

    private var context: Context
    var adArrayList: ArrayList<ModelAd>
    private var filterList: ArrayList<ModelAd>

    private var filter: FilterAd? = null


    private var firebaseAuth: FirebaseAuth

    constructor(context: Context, adArrayList: ArrayList<ModelAd>) {
        this.context = context
        this.adArrayList = adArrayList
        this.filterList = adArrayList

        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAd {
        bindingAdapterAd = RowAdBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderAd(bindingAdapterAd.root)
    }

    override fun onBindViewHolder(holder: HolderAd, position: Int) {
        //get data from list
        val modelAd =adArrayList[position]

        val title = modelAd.title
        val description = modelAd.description
        val condition = modelAd.condition
        val price = modelAd.price
        val timestamp = modelAd.timestamp
        val formattedDate = Utils.formatTimestampDate(timestamp)

        loadAdFirstImage(modelAd, holder)

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.conditionTv.text = condition
        holder.priceTv.text = price
        holder.dateTv.text = formattedDate

    }

    private fun loadAdFirstImage(modelAd: ModelAd, holder: HolderAd) {
        //load first image from available images of Ad. Ex: If there is 5 img of Ad so load first img
        val adId = modelAd.id
        Log.d(TAG, "loadAdFirstImage: adId : $adId")

        val reference = FirebaseDatabase.getInstance().getReference("Ads")

        reference.child(adId).child("Images").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener{
                //Use limitToFirst(1) for returning only 1 img
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val imageUrl = "${ds.child("imageUrl").value}"
                        Log.d(TAG, "onDataChange: imageUrl: $imageUrl")

                        try {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_image_gray)
                                .into(holder.imageIv)
                        }catch (e: Exception){

                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun getItemCount(): Int {
        return adArrayList.size
    }

    override fun getFilter(): Filter {
        //init filter obj only if it is null
        if (filter == null){
            filter = FilterAd(this, filterList)
        }

        return filter as FilterAd
    }

    inner class HolderAd(itemView: View) : RecyclerView.ViewHolder(itemView){
        //init UI Views of the row_ad
        var imageIv = bindingAdapterAd.imageIv
        var titleTv = bindingAdapterAd.titleTv
        var descriptionTv = bindingAdapterAd.descriptionTv
        var favBtn = bindingAdapterAd.favBtn
        var conditionTv = bindingAdapterAd.conditionTv
        var priceTv = bindingAdapterAd.priceTv
        var dateTv = bindingAdapterAd.dateTv
    }

}