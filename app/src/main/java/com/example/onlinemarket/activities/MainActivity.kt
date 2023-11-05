package com.example.onlinemarket.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.onlinemarket.R
import com.example.onlinemarket.Utils
import com.example.onlinemarket.databinding.ActivityMainBinding
import com.example.onlinemarket.fragments.AccountFragment
import com.example.onlinemarket.fragments.HistoryChatsFragment
import com.example.onlinemarket.fragments.HomeFragment
import com.example.onlinemarket.fragments.MyAdsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var bindingMainActivity : ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    companion object{
        private const val TAG = "MAIN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMainActivity.root)

        firebaseAuth= FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null){
            //user not logged in, show LoginOptions
            startLoginOptions()
        }else{
            updateFcmToken()
            askNotificationPermission()
        }

        showHomeFragment()

        //When you click, show fragments
        bindingMainActivity.bottomNv.setOnItemSelectedListener {item ->
            when(item.itemId){
                R.id.menu_home ->{
                    showHomeFragment()
                    true

                }
                R.id.menu_chats ->{
                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()
                        false
                    }else{
                        showChatsFragment()
                        true
                    }

                }
                R.id.menu_my_ads ->{
                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()
                        false
                    }else{
                        showMyAdsFragment()
                        true
                    }

                }
                R.id.menu_account ->{
                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()
                        false
                    }else{
                        showAccountFragment()
                        true
                    }

                }else ->{
                    false
                }
            }

        }

        bindingMainActivity.sellFab.setOnClickListener {
            val intent = Intent(this, AdCreateActivity::class.java)
            intent.putExtra("isEditMode", false)
            startActivity(intent)


        }
    }

    private fun showHomeFragment(){
        bindingMainActivity.toolbarTitleTv.text="Home"

        val fragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(bindingMainActivity.fragmentsFL.id, fragment, "HomeFragment")
        fragmentTransaction.commit()

    }

    private fun showChatsFragment(){
        bindingMainActivity.toolbarTitleTv.text="Chats"

        val fragment = HistoryChatsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(bindingMainActivity.fragmentsFL.id, fragment, "ChatsFragment")
        fragmentTransaction.commit()

    }

    private fun showMyAdsFragment(){
        bindingMainActivity.toolbarTitleTv.text="My Ads"

        val fragment = MyAdsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(bindingMainActivity.fragmentsFL.id, fragment, "MyAdsFragment")
        fragmentTransaction.commit()

    }

    private fun showAccountFragment(){
        bindingMainActivity.toolbarTitleTv.text="Account"

        val fragment = AccountFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(bindingMainActivity.fragmentsFL.id, fragment, "AccountFragment")
        fragmentTransaction.commit()

    }

    private fun startLoginOptions(){
        startActivity(Intent(this, LoginOptionsActivity::class.java))
    }

    private fun updateFcmToken(){
        val myUid = "${firebaseAuth.uid}"
        Log.d(TAG, "updateFcmToken: ")

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {fcmToken ->

                Log.d(TAG, "updateFcmToken: fcmToken $fcmToken")
                val hashMap = HashMap<String, Any>()
                hashMap["fcmToken"] = "$fcmToken"

                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(myUid)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {

                        Log.d(TAG, "updateFcmToken: FCM token update to db success")
                    }
                    .addOnFailureListener {e ->
                        Log.e(TAG, "updateFcmToken: ", )
                    }
                    
            }
            .addOnFailureListener {e ->
                Log.e(TAG, "updateFcmToken: ", )
            }

    }

    private fun askNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_DENIED){
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted ->
        Log.d(TAG, "requestNotificationPermission: isGranted: $isGranted")
    }
}
/* Steps
* 1) Enable ViewBinding
* 2) Add required color
* 3) Create menu from Bottom Navigation Menu
* 4) Handle item click event
* 5) Create required fragments
* 6) Fragment nav
* 7) Create Logins options act. Eg: GG, Phone, Email*/