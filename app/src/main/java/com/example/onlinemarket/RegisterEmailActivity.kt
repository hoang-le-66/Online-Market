@file:Suppress("DEPRECATION")

package com.example.onlinemarket

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.example.onlinemarket.databinding.ActivityRegisterEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.grpc.okhttp.internal.Util

class RegisterEmailActivity : AppCompatActivity() {

    private lateinit var bindingRegisterEmailActivity: ActivityRegisterEmailBinding

    private companion object{
        private const val TAG="REGISTER_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var email = ""
    private var password = ""
    private var confirmPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingRegisterEmailActivity= ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(bindingRegisterEmailActivity.root)


        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        bindingRegisterEmailActivity.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        bindingRegisterEmailActivity.haveAccountTv.setOnClickListener {
            onBackPressed()
        }

        bindingRegisterEmailActivity.registerBtn.setOnClickListener {
            validateData()

        }


    }
    private fun validateData(){
        email= bindingRegisterEmailActivity.emailEdt.text.toString().trim()
        password= bindingRegisterEmailActivity.passwordEdt.text.toString().trim()
        confirmPassword= bindingRegisterEmailActivity.confirmPasswordEdt.text.toString().trim()

        Log.d(TAG,"validate: email: $email")
        Log.d(TAG,"validate: password: $password")
        Log.d(TAG,"validate: confirmPassword: $confirmPassword")

        //validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            bindingRegisterEmailActivity.emailEdt.error= "Invalid Email Format"
            bindingRegisterEmailActivity.emailEdt.requestFocus()
        }else if (password.isEmpty()){
            bindingRegisterEmailActivity.passwordEdt.error= "Enter Password"
            bindingRegisterEmailActivity.passwordEdt.requestFocus()
        }else if (confirmPassword.isEmpty()){
            bindingRegisterEmailActivity.confirmPasswordEdt.error= "Enter Confirm Password"
            bindingRegisterEmailActivity.confirmPasswordEdt.requestFocus()
        }else if (password != confirmPassword){
            bindingRegisterEmailActivity.confirmPasswordEdt.error= "Password doesn't match"
            bindingRegisterEmailActivity.confirmPasswordEdt.requestFocus()
        }else{
            registerUser()
        }

    }

    private fun registerUser(){
        Log.d(TAG,"registerUser: ")
        progressDialog.setMessage("Creating account")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener{
                Log.d(TAG, "registerUser: Register Success")
                updateUserInfo()

            }.addOnFailureListener { e ->
                Log.e(TAG,"registerUser: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to create account due to ${e.message}")

            }
    }

    private fun updateUserInfo(){
        Log.d(TAG, "updateUserInfo: ")
        progressDialog.setMessage("Saving User Info")

        val timestamp = Utils.getTimeStamp()
        val registeredUserEmail= firebaseAuth.currentUser!!.email
        val registeredUserUid= firebaseAuth.uid
        //??
        val hashMap = HashMap<String,Any>()
        hashMap["name"] = ""
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Email" //possible values Phone/Email/Google
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = "$registeredUserEmail"
        hashMap["uid"] = "$registeredUserUid"

        /*Set data
        * Use registerUserUid for saving*/
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //save success
                Log.d(TAG,"updateUserInfo: User registered")

                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {e ->
                //save failed
                Log.e(TAG,"updateUserInfo: ",e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to save user info due to ${e.message}")
            }

    }
}