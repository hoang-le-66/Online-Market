package com.example.onlinemarket.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.onlinemarket.RvListenerCategory
import com.example.onlinemarket.Utils
import com.example.onlinemarket.adapters.AdapterAd
import com.example.onlinemarket.adapters.AdapterCategory
import com.example.onlinemarket.databinding.FragmentHomeBinding
import com.example.onlinemarket.models.ModelAd
import com.example.onlinemarket.models.ModelCategory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var homeFragmentBinding: FragmentHomeBinding

    private companion object{
        private const val TAG = "HOME_TAG"
    }
    //Context for this fragment class
    private lateinit var mContext: Context

    private lateinit var adArrayList: ArrayList<ModelAd>
    private lateinit var adapterAd: AdapterAd

    /*private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private var currentAddress = 0.0*/
    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        homeFragmentBinding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext),container,false)
        return homeFragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCategories()

        loadAds("All")
        //add text change listener to searchEdt to search ads base on query type
        homeFragmentBinding.searchEdt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: Query: $s")
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

    private fun loadAds(category: String) {
        Log.d(TAG, "loadAds: category: $category")

        adArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.addValueEventListener(object : ValueEventListener{
            @SuppressLint("SuspiciousIndentation")
            override fun onDataChange(snapshot: DataSnapshot) {
                adArrayList.clear()

                for (ds in snapshot.children){

                    try {
                        val modelAd = ds.getValue(ModelAd::class.java)

                            // Add Ad to list if category is matched
                            if(category == "All"){
                                adArrayList.add(modelAd!!)
                            } else {
                                // Some category is selected
                                if (modelAd!!.category.equals(category)){
                                    // Selected category is matched with Ad's category
                                    adArrayList.add(modelAd)
                                }
                            }

                    } catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }
                //setup Adapter
                adapterAd = AdapterAd(mContext, adArrayList)
                homeFragmentBinding.adsRv.adapter = adapterAd

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadCategories(){
        val categoryArrayList = ArrayList<ModelCategory>()

        for(i in 0 until Utils.categories.size){
            val modelCategory = ModelCategory(Utils.categories[i], Utils.categoryIcons[i])
            categoryArrayList.add(modelCategory)
        }
        //set up AdapterCategory
        val adapterCategory = AdapterCategory(mContext, categoryArrayList, object:
            RvListenerCategory {
            override fun onCategoryClick(modelCategory: ModelCategory) {
                //get selected category
                val selectedCategory = modelCategory.category
                //load ads based on selected category
                loadAds(selectedCategory)
            }
        })
        //set adapter to RecyclerView
        homeFragmentBinding.categoriesRv.adapter = adapterCategory
    }

}