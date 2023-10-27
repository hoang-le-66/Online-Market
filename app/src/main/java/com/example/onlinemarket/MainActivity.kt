package com.example.onlinemarket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.onlinemarket.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import io.grpc.okhttp.internal.Util

class MainActivity : AppCompatActivity() {

    private lateinit var bindingMainActivity : ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMainActivity.root)

        firebaseAuth= FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null){
            //user not logged in, show LoginOptions
            startLoginOptions()
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
                        Utils.toast(this,"Login Required")
                        startLoginOptions()
                        false
                    }else{
                        showChatsFragment()
                        true
                    }

                }
                R.id.menu_my_ads ->{
                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this,"Login Required")
                        startLoginOptions()
                        false
                    }else{
                        showMyAdsFragment()
                        true
                    }

                }
                R.id.menu_account ->{
                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this,"Login Required")
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
            startActivity(Intent(this,AdCreateActivity::class.java))

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

        val fragment = ChatsFragment()
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
        startActivity(Intent(this,LoginOptionsActivity::class.java))
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