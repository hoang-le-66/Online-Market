package com.example.onlinemarket.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.onlinemarket.R
import com.example.onlinemarket.Utils
import com.example.onlinemarket.adapters.AdapterAd
import com.example.onlinemarket.databinding.ActivityAdSellerProfileBinding
import com.example.onlinemarket.models.ModelAd
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdSellerProfileActivity : AppCompatActivity() {

    private lateinit var bindingAdSellerProfile: ActivityAdSellerProfileBinding

    private companion object{
        private const val TAG = "SELLER_PROFILE_TAG"
    }

    private var sellerUid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingAdSellerProfile = ActivityAdSellerProfileBinding.inflate(layoutInflater)
        setContentView(bindingAdSellerProfile.root)

        //get uid of the seller (send data from AdDetails)
        sellerUid = intent.getStringExtra("sellerUid").toString()
        Log.d(TAG, "onCreate: sellerUid : $sellerUid")

        loadSellerDeails()

        loadAds()

        bindingAdSellerProfile.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadSellerDeails(){
        Log.d(TAG, "loadSellerDeails: ")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(sellerUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    //Error casting here
                    //former code
                    /*val timestamp = "${snapshot.child("timestamp").value}" as Long*/
                    val timestamp = snapshot.child("timestamp").value as Long

                    val formattedDate = Utils.formatTimestampDate(timestamp)

                    bindingAdSellerProfile.sellerNameTv.text = name
                    bindingAdSellerProfile.sellerMemberSinceTv.text = formattedDate
                    try {
                        Glide.with(this@AdSellerProfileActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.icon_person_white)
                            .into(bindingAdSellerProfile.sellerProfileIv)

                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadAds(){
        Log.d(TAG, "loadAds: ")

        val adArrayList: ArrayList<ModelAd> = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")

        ref.orderByChild("uid").equalTo(sellerUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    adArrayList.clear()

                    for (ds in snapshot.children){
                        try {
                            val modelAd = ds.getValue(ModelAd::class.java)

                            adArrayList.add(modelAd!!)

                        }catch (e: Exception){
                            Log.e(TAG, "onDataChange: ", e)
                        }

                        val adapterAd = AdapterAd(this@AdSellerProfileActivity, adArrayList)
                        bindingAdSellerProfile.adsRv.adapter = adapterAd

                        val adsCount = "${adArrayList.size}"
                        bindingAdSellerProfile.publishedAdsCountTv.text = adsCount

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}
