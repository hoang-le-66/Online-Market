package com.example.onlinemarket

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.widget.Toast
import androidx.core.content.contentValuesOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.HashMap

object Utils {

    //Constant to define Ads status. When ad is published the Ad status will be set AVAILABLE
    const val AD_STATUS_AVAILABLE = "AVAILABLE"
    const val AD_STATUS_SOLD = "SOLD"

    const val MESSAGE_TYPE_TEXT = "TEXT"
    const val MESSAGE_TYPE_IMAGE = "IMAGE"

    const val NOTIFICATION_TYPE_NEW_MESSAGE = "NOTIFICATION_TYPE_NEW_MESSAGE"
    const val FCM_SERVER_KEY = "AAAAZCGYODY:APA91bFdLF3n84QHc-6LOUhUyf11StSqMJRIUy-1dqaYgQ_45_NEVEb83qjmlAT6ck7bF1BXA_TnX5sVrudIKhrHNy5-LDDI_fxpXsr7lX4Gn-Y4O7dIDr-1npkVxppctv6pZf8s3dvp"


    //Categories array of the Ads
    val categories = arrayOf(
        "All",
        "Mobiles",
        "Computer/Laptop",
        "Electronics & Home Appliances",
        "Vehicles",
        "Furniture & Home Decor",
        "Fashion & Beauty",
        "Books",
        "Sports",
        "Animals",
        "Businesses",
        "Agriculture"
    )
    val categoryIcons = arrayOf(
        R.drawable.ic_category_all,
        R.drawable.ic_category_mobile,
        R.drawable.ic_category_computer,
        R.drawable.ic_category_electronic,
        R.drawable.ic_category_vehicle,
        R.drawable.ic_category_furniture,
        R.drawable.ic_category_fashion,
        R.drawable.ic_category_book,
        R.drawable.ic_category_sport,
        R.drawable.ic_category_animal,
        R.drawable.ic_category_business,
        R.drawable.ic_category_agriculture
    )
    //Ad product conditions. Ex: New, Used, Refurbished
    val conditions = arrayOf(
        "New",
        "Used",
        "Refurbished"
    )

    fun toast(context: Context, message: String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    fun getTimeStamp(): Long{
        return System.currentTimeMillis()
    }

    fun formatTimestampDate(timestamp: Long): String{

        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }

    fun formatTimestampMessage(timestamp: Long): String{

        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString()
    }

    fun addToFavourite(context: Context, adId: String){

        val firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser == null){
            Utils.toast(context, "You're not logged-in!!")

        }else{
            val timestamp = Utils.getTimeStamp()

            val hashMap = HashMap<String, Any>()
            hashMap["adId"] = adId
            hashMap["timestamp"] = timestamp

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favourites").child(adId)
                .setValue(hashMap)
                .addOnSuccessListener {
                    Utils.toast(context, "Added to Favourites")
                }.addOnFailureListener {e ->
                    Utils.toast(context, "Failed to add to favourite due to ${e.message} ")
                }
        }
    }

    fun removeFromFavourite(context: Context, adId: String){
        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null){
            Utils.toast(context, "You're not logged-in!")
        }else{
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favourites").child(adId)
                .removeValue()
                .addOnSuccessListener {
                    Utils.toast(context,"Remove from favourite")
                }
                .addOnFailureListener {e->
                    Utils.toast(context,"Failed to remove from favourite due to ${e.message}")
                }
        }
    }
    /*
    * Launch Call Intent with phone number
    * @param context : context of act/fragment from where this func will be called
    * @param phone: the phone number that will be opened in call intent*/
    fun callIntent(context: Context, phone: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:${Uri.encode(phone)}"))
        context.startActivity(intent)

    }
    /*
   * Launch Sms Intent with phone number
   * @param context : context of act/fragment from where this func will be called
   * @param phone: the phone number that will be opened in sms intent*/
    fun smsIntent(context: Context, phone: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${Uri.encode(phone)}"))
        context.startActivity(intent)
    }

    /*Launch Map Intent*/
    fun mapIntent(context: Context, latitude: Double, longitude: Double){
        val gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=$latitude,$longitude")

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null){
            context.startActivity(mapIntent)
        }else{
            Utils.toast(context, "GG map not installed")
        }
    }

    /*Generate chat Path by sorting theses UIDs and concatenate sorted array of UIDs having _ in between
    *It kind of a room chat Id and it is unique
    * All messages of these 2 users will be save in path
    *@param receiptUid: Receiver Uid
    *@param yourUid: UID current logged-in user
    * */
    fun chatPath(receiptUid: String, yourUid: String): String{
        var arrayUids = arrayOf(receiptUid, yourUid)
        //Sort Array
        Arrays.sort(arrayUids)
        //Concatenate both UIDs
        //return chatPath. Ex: Send= A and Receive = B so chatPath= A_B
        return "${arrayUids[0]}_${arrayUids[1]}}"

    }
}