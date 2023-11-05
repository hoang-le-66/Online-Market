package com.example.onlinemarket.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.example.onlinemarket.R
import com.example.onlinemarket.Utils
import com.example.onlinemarket.adapters.AdapterImageSlider
import com.example.onlinemarket.databinding.ActivityAdDetailsBinding
import com.example.onlinemarket.models.ModelAd
import com.example.onlinemarket.models.ModelImageSlider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdDetailsActivity : AppCompatActivity() {

    private lateinit var bindingAdDetails: ActivityAdDetailsBinding

    private companion object{
        private const val TAG = "AD_DETAILS_TAG"
    }
    private lateinit var firebaseAuth: FirebaseAuth

    private var adId = ""

    private var adLatitude = 0.0
    private var adLongitude = 0.0

    private var sellerUid = ""
    private var sellerPhone = ""

    private var favourite = false

    private lateinit var imageSliderArrayList: ArrayList<ModelImageSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingAdDetails = ActivityAdDetailsBinding.inflate(layoutInflater)
        setContentView(bindingAdDetails.root)
        /*hide some UI when start
         show Edit, Delete option for Ad owner
         show Call, Chat, SMS for customer*/
        bindingAdDetails.toolbarEditBtn.visibility = View.GONE
        bindingAdDetails.toolbarDeleteBtn.visibility = View.GONE
        bindingAdDetails.chatBtn.visibility = View.GONE
        bindingAdDetails.callBtn.visibility = View.GONE
        bindingAdDetails.smsBtn.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()

        adId = intent.getStringExtra("adId").toString()
        Log.d(TAG, "onCreate: adId: $adId")

        if (firebaseAuth.currentUser != null){
            checkIsFavourite()
        }

        loadAdDetails()
        loadAdImages()

        bindingAdDetails.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        bindingAdDetails.toolbarDeleteBtn.setOnClickListener {
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)

            materialAlertDialogBuilder.setTitle("Delete Ad")
                .setMessage("Are you sure to delete this Ad")
                .setPositiveButton("DELETE"){ dialog, which ->
                    Log.d(TAG, "onCreate: DELETE clicked...")
                    deletedAd()
                }.setNegativeButton("CANCEL"){ dialog, which ->
                    Log.d(TAG, "onCreate: CANCEL clicked...")
                    dialog.dismiss()

                }.show()
        }

        bindingAdDetails.toolbarEditBtn.setOnClickListener {
            editOptionsDialog()

        }

        bindingAdDetails.toolbarFavBtn.setOnClickListener {
            if (favourite){
                Utils.removeFromFavourite(this,adId)
            }else{
                Utils.addToFavourite(this,adId)
            }

        }

        bindingAdDetails.sellerProfileCv.setOnClickListener {
            val intent = Intent(this, AdSellerProfileActivity::class.java)

            intent.putExtra("sellerUid", sellerUid)
            startActivity(intent)
        }

        bindingAdDetails.chatBtn.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiptUid", sellerUid)
            startActivity(intent)
        }

        bindingAdDetails.callBtn.setOnClickListener {
            Utils.callIntent(this, sellerPhone)
        }

        bindingAdDetails.smsBtn.setOnClickListener {
            Utils.smsIntent(this, sellerPhone)
        }

        bindingAdDetails.mapBtn.setOnClickListener {
            Utils.mapIntent(this, adLatitude, adLongitude)
        }
    }

    private fun editOptionsDialog(){
        Log.d(TAG, "editOptionsDialog: ")

        val popupMenu = PopupMenu(this, bindingAdDetails.toolbarEditBtn)

        popupMenu.menu.add(Menu.NONE, 0, 0, "Edit")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Mark as sold")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {menuItem ->
            val itemId = menuItem.itemId
            if (itemId == 0){

                val intent = Intent(this, AdCreateActivity::class.java)
                intent.putExtra("isEditMode", true)
                intent.putExtra("adId", adId)
                startActivity(intent)

            } else if (itemId ==1){
                showMarkAsSoldDialog()
            }

            return@setOnMenuItemClickListener true

        }
    }

    private fun showMarkAsSoldDialog(){
        Log.d(TAG, "showMarkAsSoldDialog: ")
        //Alert Dialog
        val alertDialogBuilder = MaterialAlertDialogBuilder(this)
        alertDialogBuilder.setTitle("Mark as sold?")
            .setMessage("Are you sure to mark this AD as sold?")
            .setPositiveButton("SOLD"){dialog, which ->
                Log.d(TAG, "showMarkAsSoldDialog: SOLD clicked")

                val hashMap = HashMap<String, Any>()
                hashMap["status"] = "${Utils.AD_STATUS_SOLD}"

                val ref = FirebaseDatabase.getInstance().getReference("Ads")
                ref.child(adId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        Log.d(TAG, "showMarkAsSoldDialog: Marked as sold")
                    }
                    .addOnFailureListener {e ->
                        Log.e(TAG, "showMarkAsSoldDialog: ", e)
                        Utils.toast(this, "Failed to mark as sold due to ${e.message}")
                    }

            }
            .setNegativeButton("CANCEL"){ dialog, which ->
                Log.d(TAG, "showMarkAsSoldDialog: CANCEL clicked")
                dialog.dismiss()
            }
            .show()
    }

    private fun loadAdDetails(){
        Log.d(TAG, "loadAdDetails: ")

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    try {
                        val modelAd = snapshot.getValue(ModelAd::class.java)
                        sellerUid = "${modelAd!!.uid}"
                        val title = modelAd.title
                        val description = modelAd.description
                        val address = modelAd.address
                        val condition = modelAd.condition
                        val category = modelAd.category
                        val price = modelAd.price
                        adLatitude = modelAd.latitude
                        adLongitude = modelAd.longitude
                        val timestamp = modelAd.timestamp

                        val formattedDate = Utils.formatTimestampDate(timestamp)

                        if (sellerUid == firebaseAuth.uid){
                           /* Ad is created by current sigh-in user
                           * 1) Able to edit and delete Ad*/
                            bindingAdDetails.toolbarEditBtn.visibility = View.VISIBLE
                            bindingAdDetails.toolbarDeleteBtn.visibility = View.VISIBLE
                            /*2)Not able to chat, call, sms to yourself*/
                            bindingAdDetails.chatBtn.visibility = View.GONE
                            bindingAdDetails.callBtn.visibility = View.GONE
                            bindingAdDetails.smsBtn.visibility = View.GONE

                            bindingAdDetails.sellerProfileLabelTv.visibility = View.GONE
                            bindingAdDetails.sellerProfileCv.visibility = View.GONE

                        }else{
                            bindingAdDetails.toolbarEditBtn.visibility = View.GONE
                            bindingAdDetails.toolbarDeleteBtn.visibility = View.GONE

                            bindingAdDetails.chatBtn.visibility = View.VISIBLE
                            bindingAdDetails.callBtn.visibility = View.VISIBLE
                            bindingAdDetails.smsBtn.visibility = View.VISIBLE

                            bindingAdDetails.sellerProfileLabelTv.visibility = View.VISIBLE
                            bindingAdDetails.sellerProfileCv.visibility = View.VISIBLE

                        }

                        bindingAdDetails.titleTv.text = title
                        bindingAdDetails.descriptionTv.text = description
                        bindingAdDetails.addressTv.text = address
                        bindingAdDetails.conditionTv.text = condition
                        bindingAdDetails.categoryTv.text = category
                        bindingAdDetails.priceTv.text = price
                        bindingAdDetails.dateTv.text = formattedDate

                        loadSellerDetails()

                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadSellerDetails() {
        Log.d(TAG, "loadSellerDetails: ")
        //Db path to load Seller info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(sellerUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    val timestamp = snapshot.child("timestamp").value as Long

                    val formattedDate = Utils.formatTimestampDate(timestamp)

                    sellerPhone = "$phoneCode$phoneNumber"

                    bindingAdDetails.sellerNameTv.text = name
                    bindingAdDetails.memberSinceTv.text = formattedDate
                    try {
                        Glide.with(this@AdDetailsActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.icon_person_white)
                            .into(bindingAdDetails.sellerProfileIv)
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkIsFavourite(){
        Log.d(TAG, "checkIsFavourite: ")
        //Db path to check if Ad is in Fav or current user.
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}").child("Favourites").child(adId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    favourite = snapshot.exists()
                    Log.d(TAG, "onDataChange: favourite: $favourite")

                    if (favourite){
                        bindingAdDetails.toolbarFavBtn.setImageResource(R.drawable.ic_fav_yes)

                    }else{
                        bindingAdDetails.toolbarFavBtn.setImageResource(R.drawable.ic_fav_no)
                    }
                    
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadAdImages(){
        Log.d(TAG, "loadAdImages: ")

        imageSliderArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId).child("Images")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    imageSliderArrayList.clear()

                    for (ds in snapshot.children){

                        try {
                            val modelImageSlider = ds.getValue(ModelImageSlider::class.java)
                            imageSliderArrayList.add(modelImageSlider!!)
                        }catch (e: Exception){
                            Log.e(TAG, "onDataChange: ", e)
                        }

                    }

                    val adapterImageSlider = AdapterImageSlider(this@AdDetailsActivity, imageSliderArrayList)
                    bindingAdDetails.imageSliderVp.adapter = adapterImageSlider

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }

    private fun deletedAd(){
        Log.d(TAG, "deletedAd: ")

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "deletedAd: Deleted")
                Utils.toast(this, "Deleted...!")

                finish()
            }
            .addOnFailureListener {e ->
                Log.e(TAG, "deletedAd: ", e)
                Utils.toast(this, "Failed to deleted due to ${e.message}")
                
            }
    }

}