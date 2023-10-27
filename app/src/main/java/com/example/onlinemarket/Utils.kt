package com.example.onlinemarket

import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import java.security.Timestamp
import java.util.*

object Utils {

    //Constant to define Ads status. When ad is published the Ad status will be set AVAILABLE
    const val AD_STATUS_AVAILABLE = "AVAILABLE"
    const val AD_STATUS_SOLD = "SOLD"
    //Categories array of the Ads
    val categories = arrayOf(
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

}