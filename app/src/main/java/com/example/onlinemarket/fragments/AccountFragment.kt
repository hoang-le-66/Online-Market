package com.example.onlinemarket.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.onlinemarket.activities.MainActivity
import com.example.onlinemarket.activities.ProfieEditActivity
import com.example.onlinemarket.R
import com.example.onlinemarket.Utils
import com.example.onlinemarket.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountFragment : Fragment() {


    private lateinit var bindingFragmentAccount: FragmentAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context

    private companion object{
        private const val TAG = "ACCOUNT_TAG"
    }

    override fun onAttach(context: Context) {
        mContext = context
        firebaseAuth = FirebaseAuth.getInstance()
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingFragmentAccount = FragmentAccountBinding.inflate(layoutInflater)

        return bindingFragmentAccount.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadMyInfo()

        firebaseAuth = FirebaseAuth.getInstance()

        bindingFragmentAccount.logOutCv.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(mContext, MainActivity::class.java))
            activity?.finishAffinity()
        }

        bindingFragmentAccount.editProfileCv.setOnClickListener {
            startActivity(Intent(mContext, ProfieEditActivity::class.java))

        }
    }

    private fun loadMyInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dob = "${snapshot.child("dob").value}"
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    val userType = "${snapshot.child("phoneCode").value}"

                    val phone = phoneCode+phoneNumber

                    if (timestamp == "null"){
                        timestamp = "0"
                    }
                    //format timestamp
                    val formattedDate = Utils.formatTimestampDate(timestamp.toLong())
                    //set data to UI
                    bindingFragmentAccount.emailTv.text = email
                    bindingFragmentAccount.fullNameTv.text = email
                    bindingFragmentAccount.dobTv.text = dob
                    bindingFragmentAccount.phoneTv.text = phone
                    bindingFragmentAccount.memberSinceTv.text = formattedDate

                    if (userType == "Email"){
                        val isVerified= firebaseAuth.currentUser!!.isEmailVerified
                        if (isVerified){
                            bindingFragmentAccount.verificationTv.text= "Verified"
                        }else{
                            bindingFragmentAccount.verificationTv.text= "Not Verified"

                        }

                    }else{
                        bindingFragmentAccount.verificationTv.text= "Verified"
                    }
                    //Set profile img
                    try {
                        Glide.with(mContext)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.icon_person_white)
                            .into(bindingFragmentAccount.profileIv)

                    }
                    catch (e: Exception){
                        Log.e(TAG, "onDataChange: ",e )
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

}