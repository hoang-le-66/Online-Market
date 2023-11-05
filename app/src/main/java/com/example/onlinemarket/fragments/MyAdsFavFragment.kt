package com.example.onlinemarket.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.onlinemarket.adapters.AdapterAd
import com.example.onlinemarket.databinding.FragmentMyAdsFavBinding
import com.example.onlinemarket.models.ModelAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MyAdsFavFragment : Fragment() {

    private lateinit var bindingAdsFavFragment: FragmentMyAdsFavBinding

    companion object{
        private const val TAG = "FAV_ADS_TAG"

    }

    private lateinit var mContext: Context

    private lateinit var firebaseAuth: FirebaseAuth

    //adArrayList to hold ads list added to fav by currently logged-in user to show
    private lateinit var adArrayList: ArrayList<ModelAd>

    //AdapterAd class instance to set to RecycleView to show Ads List
    private lateinit var adapterAd: AdapterAd


    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bindingAdsFavFragment = FragmentMyAdsFavBinding.inflate(inflater, container, false)
        return bindingAdsFavFragment.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        loadAds()

        bindingAdsFavFragment.searchEdt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                try {
                    val query = s.toString()
                    adapterAd.filter.filter(query)

                }catch (e: Exception){
                    Log.e(TAG, "onTextChanged: ", e)

                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

    }

    private fun loadAds(){
        Log.d(TAG, "loadAds: ")

        adArrayList = ArrayList()

        val favRef = FirebaseDatabase.getInstance().getReference("Users")
        favRef.child(firebaseAuth.uid!!).child("Favourites")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    adArrayList.clear()

                    for(ds in snapshot.children){
                        val adId = "${ds.child("adId").value}"
                        Log.d(TAG, "onDataChange: adId: $adId")

                        val adRef = FirebaseDatabase.getInstance().getReference("Ads")
                        adRef.child(adId)
                            .addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    try {
                                        val modelAd = snapshot.getValue(ModelAd::class.java)

                                        adArrayList.add(modelAd!!)
                                    }catch (e: Exception){
                                        Log.e(TAG, "onDataChange: ", e)
                                    }

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                    }

                    Handler().postDelayed({
                        adapterAd = AdapterAd(mContext, adArrayList)
                        bindingAdsFavFragment.adsRv.adapter = adapterAd

                    },500)


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


    }



}