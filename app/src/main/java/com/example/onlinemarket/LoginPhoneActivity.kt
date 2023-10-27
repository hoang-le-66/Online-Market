@file:Suppress("DEPRECATION")

package com.example.onlinemarket

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.util.Util
import com.example.onlinemarket.databinding.ActivityLoginPhoneBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class LoginPhoneActivity : AppCompatActivity() {

    private lateinit var bindingLoginPhone: ActivityLoginPhoneBinding

    private companion object{
        private const val TAG = "PHONE_LOGIN_TAG"
    }

    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    private var forceRefreshingToken: ForceResendingToken? = null
    private lateinit var mCallbacks: OnVerificationStateChangedCallbacks

    private var mVerificationId: String? = null

    private var phoneCode=""
    private var phoneNumber=""
    private var phoneNumberWithCode=""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingLoginPhone = ActivityLoginPhoneBinding.inflate(layoutInflater)
        setContentView(bindingLoginPhone.root)

        bindingLoginPhone.phoneInputRL.visibility = View.VISIBLE
        bindingLoginPhone.otpInputRL.visibility = View.GONE

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth= FirebaseAuth.getInstance()

        phoneLoginCallBacks()


        bindingLoginPhone.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        bindingLoginPhone.sendOtpBtn.setOnClickListener {
            validateData()

        }

        bindingLoginPhone.resendOTP.setOnClickListener {
            resendVerificationCode(forceRefreshingToken)

        }

        bindingLoginPhone.verifyOtpBtn.setOnClickListener {
            val otp = bindingLoginPhone.otpEdt.toString().trim()
            Log.d(TAG, "onCreate: otp: $otp")

            if (otp.isEmpty()){
                bindingLoginPhone.otpEdt.error = "Enter OTP"
                bindingLoginPhone.otpEdt.requestFocus()
            }else if(otp.length < 6){
                bindingLoginPhone.otpEdt.error = "OTP length must be 6 characters long"
                bindingLoginPhone.otpEdt.requestFocus()
            }else{
                verifyPhoneNumberWithCode(mVerificationId, otp)
            }


        }

    }



    private fun phoneLoginCallBacks() {
        Log.d(TAG,"phoneLoginCallBacks: ")

        mCallbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG,"onVerificationCompleted: ")
                signInWithPhoneAuthCredential(credential)

            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG,"onVerificationFailed",e)

                progressDialog.dismiss()
                Utils.toast(this@LoginPhoneActivity,"${e.message}")

            }

            override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
                Log.d(TAG, "onCodeSent: verificationId: $verificationId")
                mVerificationId = verificationId
                forceRefreshingToken = token

                progressDialog.dismiss()
                bindingLoginPhone.phoneInputRL.visibility = View.GONE
                bindingLoginPhone.otpInputRL.visibility = View.VISIBLE

                Utils.toast(this@LoginPhoneActivity, "OTP is sent to $phoneNumberWithCode")
                bindingLoginPhone.loginLabelTV.text = "Please type the verification code sent to $phoneNumberWithCode"
            }

            override fun onCodeAutoRetrievalTimeOut(credential: String) {

            }

        }
    }



    private fun validateData(){
        phoneCode = bindingLoginPhone.phoneCodeTil.selectedCountryCodeWithPlus
        phoneNumber = bindingLoginPhone.phoneNumberEt.text.toString().trim()
        phoneNumberWithCode = phoneCode + phoneNumber

        Log.d(TAG, "validateData: phoneCode: $phoneCode")
        Log.d(TAG, "validateData: phoneNumber: $phoneNumber")
        Log.d(TAG, "validateData: phoneNumberWithCode: $phoneNumberWithCode")

        if (phoneNumber.isEmpty()){
            bindingLoginPhone.phoneNumberEt.error = "Enter Phone Number"
            bindingLoginPhone.phoneNumberEt.requestFocus()
        }else{
            startPhoneNumberVerification()
        }

    }

    private fun startPhoneNumberVerification() {
        Log.d(TAG, "startPhoneNumberVerification: ")

        progressDialog.setMessage("Sending OTP to $phoneNumberWithCode")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, otp: String) {
        Log.d(TAG, "verifyPhoneNumberWithCode: mVerificationId: $verificationId")
        Log.d(TAG, "verifyPhoneNumberWithCode: otp: $otp")

        progressDialog.setMessage("Verifying OTP")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId!!,otp)
        signInWithPhoneAuthCredential(credential)

    }

    private fun resendVerificationCode(token: ForceResendingToken?){
        Log.d(TAG, "resendVerificationCode: ")

        progressDialog.setMessage("Resending OTP to $phoneNumberWithCode")
        progressDialog.show()
        //set up phone auth options
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks)
            .setForceResendingToken(token!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: ")

        progressDialog.setMessage("Logging In")
        progressDialog.show()

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {authResult ->
                Log.d(TAG, "signInWithPhoneAuthCredential: Success")

                if (authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "signInWithPhoneAuthCredential: New User, Account created")
                    updateUserInfoDb()
                }else{
                    Log.d(TAG, "signInWithPhoneAuthCredential: Existing User Logged In")
                    //Existed User, no need to save to Db
                    startActivity(Intent(this,MainActivity::class.java))
                    finishAffinity()
                }

            }.addOnFailureListener {e ->
                Log.e(TAG, "signInWithPhoneAuthCredential: ", )
                progressDialog.dismiss()
                Utils.toast(this, "Failed to login due to ${e.message}")

            }
    
    }

    private fun updateUserInfoDb() {
        Log.d(TAG, "updateUserInfoDb: ")

        progressDialog.setMessage("Saving User Info")
        progressDialog.show()

        val timestamp = Utils.getTimeStamp()
        val registeredUserUid = firebaseAuth.uid

        val hashMap = HashMap<String,Any?>()
        hashMap["name"] = ""
        hashMap["phoneCode"] = "$phoneCode"
        hashMap["phoneNumber"] = "$phoneNumber"
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Phone" //possible values Phone/Email/Google
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = ""
        hashMap["uid"] = registeredUserUid

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