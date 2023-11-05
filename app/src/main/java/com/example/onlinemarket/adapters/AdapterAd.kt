package com.example.onlinemarket.adapters

import android.content.Context
import android.content.Intent
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
import com.example.onlinemarket.activities.AdDetailsActivity
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
        val address = modelAd.address
        val condition = modelAd.condition
        val price = modelAd.price
        val timestamp = modelAd.timestamp
        val formattedDate = Utils.formatTimestampDate(timestamp)

        loadAdFirstImage(modelAd, holder)

        if (firebaseAuth.currentUser != null){
            checkIsFavourite(modelAd, holder)
        }

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.addressTv.text = address
        holder.conditionTv.text = condition
        holder.priceTv.text = price
        holder.dateTv.text = formattedDate

        holder.itemView.setOnClickListener {
            val intent = Intent(context, AdDetailsActivity::class.java)
            intent.putExtra("adId", modelAd.id)
            context.startActivity(intent)

        }

        holder.favBtn.setOnClickListener {
            val favourite = modelAd.favourite
            if (favourite){
                Utils.removeFromFavourite(context, modelAd.id)
            }else{
                Utils.addToFavourite(context, modelAd.id)
            }
        }

    }

    private fun checkIsFavourite(modelAd: ModelAd, holder: AdapterAd.HolderAd) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favourites").child(modelAd.id)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favourite = snapshot.exists()

                    modelAd.favourite = favourite
                    if (favourite){
                        holder.favBtn.setImageResource(R.drawable.ic_fav_yes)
                    }else{
                        holder.favBtn.setImageResource(R.drawable.ic_fav_no)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

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
                            Log.e(TAG, "onDataChange: ", e)
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
        var addressTv = bindingAdapterAd.addressTv
        var favBtn = bindingAdapterAd.favBtn
        var conditionTv = bindingAdapterAd.conditionTv
        var priceTv = bindingAdapterAd.priceTv
        var dateTv = bindingAdapterAd.dateTv
    }

}