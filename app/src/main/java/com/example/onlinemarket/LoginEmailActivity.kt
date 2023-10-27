@file:Suppress("DEPRECATION")

package com.example.onlinemarket

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.onlinemarket.databinding.ActivityLoginEmailBinding
import com.google.firebase.auth.FirebaseAuth

class LoginEmailActivity : AppCompatActivity() {

    private lateinit var bindingLoginEmailActivity: ActivityLoginEmailBinding
    private var email=""
    private var password=""
    //??
    private companion object{
        private const val TAG = "LOGIN_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingLoginEmailActivity= ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(bindingLoginEmailActivity.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        //When touch outside Dialog, nothing happen
        progressDialog.setCanceledOnTouchOutside(false)


        bindingLoginEmailActivity.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        bindingLoginEmailActivity.noAccountTv.setOnClickListener {
            startActivity(Intent(this,RegisterEmailActivity::class.java))

        }

        bindingLoginEmailActivity.loginBtn.setOnClickListener {
            validateData()
        }

    }
    private fun validateData(){
        email= bindingLoginEmailActivity.emailEdt.text.toString().trim()
        password= bindingLoginEmailActivity.passwordEdt.text.toString().trim()

        Log.d(TAG,"validateData: email: $email")
        Log.d(TAG,"validateData: password: $password")

        //validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            bindingLoginEmailActivity.emailEdt.error= "Invalid Email Format"
            bindingLoginEmailActivity.emailEdt.requestFocus()
        }else if (password.isEmpty()){
            bindingLoginEmailActivity.passwordEdt.error= "Enter Password"
            bindingLoginEmailActivity.passwordEdt.requestFocus()
        }else{
            loginUser()
        }
    }

    private fun loginUser(){
        Log.d(TAG,"loginUser: ")
        //show progress
        progressDialog.setMessage("Logging In")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                Log.d(TAG,"loginUser: Logged In...")
                progressDialog.dismiss()

                startActivity(Intent(this, MainActivity::class.java))
                //finish current act and all act in back stack
                finishAffinity()

            }.addOnFailureListener {e ->
                Log.e(TAG,"loginUser: ",e)
                progressDialog.dismiss()

                Utils.toast(this,"Unable to login due to ${e.message}")
            }
    }
}