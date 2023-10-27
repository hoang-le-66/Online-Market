@file:Suppress("DEPRECATION")

package com.example.onlinemarket

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.onlinemarket.databinding.ActivityLoginOptionsBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginOptionsActivity : AppCompatActivity() {

    private lateinit var bindingLoginOptions: ActivityLoginOptionsBinding

    private companion object{
        private const val TAG = "LOGIN_OPTIONS_TAG"

    }
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingLoginOptions= ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(bindingLoginOptions.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth= FirebaseAuth.getInstance()
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.id.defa))
        //handle closeBtn click, go back
        bindingLoginOptions.closeBtn.setOnClickListener {
            onBackPressed()
        }

        bindingLoginOptions.loginEmailBtn.setOnClickListener {
            startActivity(Intent(this,LoginEmailActivity::class.java))
        }

        bindingLoginOptions.loginPhoneBtn.setOnClickListener {
            startActivity(Intent(this,LoginPhoneActivity::class.java))
        }
    }

    private fun updateUserInfoDb() {
        Log.d(TAG, "updateUserInfoDb: ")

        progressDialog.setMessage("Saving User Info")
        progressDialog.show()

        val timestamp = Utils.getTimeStamp()
        val registeredUserEmail = firebaseAuth.uid
        val registeredUserUid = firebaseAuth.uid
        val name = firebaseAuth.currentUser?.displayName

        val hashMap = HashMap<String,Any?>()
        hashMap["name"] = "$name"
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Google" //possible values Phone/Email/Google
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = registeredUserEmail
        hashMap["uid"] = registeredUserUid

        //set data to Db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {

                Log.d(TAG, "updateUserInfoDb: User info saved ")
                progressDialog.dismiss()

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()

            }.addOnFailureListener {e ->
                Log.e(TAG, "updateUserInfoDb: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to save user info due to ${e.message}")

            }

    }
}