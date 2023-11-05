package com.example.onlinemarket.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.onlinemarket.databinding.FragmentMyAdsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class MyAdsFragment : Fragment() {
    private lateinit var myAdsBinding: FragmentMyAdsBinding

    private lateinit var myTabsViewPagerAdapter: MyTabsViewPagerAdapter

    private companion object{
        private const val TAG = "MY_ADS_TAG"
    }

    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myAdsBinding = FragmentMyAdsBinding.inflate(inflater, container, false)

        return myAdsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myAdsBinding.tabLayout.addTab(myAdsBinding.tabLayout.newTab().setText("Ads"))
        myAdsBinding.tabLayout.addTab(myAdsBinding.tabLayout.newTab().setText("Favourites"))

        val fragmentManager = childFragmentManager
        myTabsViewPagerAdapter = MyTabsViewPagerAdapter(fragmentManager, lifecycle)
        myAdsBinding.viewPager.adapter = myTabsViewPagerAdapter

        myAdsBinding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {

                Log.d(TAG, "onTabSelected: tab: ${tab.position}")
                myAdsBinding.viewPager.currentItem = tab.position
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }
        })

        myAdsBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                myAdsBinding.tabLayout.selectTab(myAdsBinding.tabLayout.getTabAt(position))
            }
        })
    }

    class MyTabsViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle)
        : FragmentStateAdapter(fragmentManager, lifecycle){
        override fun createFragment(position: Int): Fragment {
            if (position == 0){
                return MyAdsAdsFragment()
            }else{
                return MyAdsFavFragment()
            }
        }

        override fun getItemCount(): Int {
            //setting static size 2 because have 2 tabs/fragment
            return 2
        }
    }

}