package com.example.onlinemarket

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.onlinemarket.databinding.ActivityProfieEditBinding
import com.google.android.datatransport.cct.internal.LogEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlin.math.log

class ProfieEditActivity : AppCompatActivity() {

    private lateinit var bindingProfileEdit: ActivityProfieEditBinding
    companion object{
        private const val TAG = "PROFILE_EDIT_TAG"
    }
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var myUserType = ""

    private var imageUri: Uri? = null

    //Using for update Data
    private var name= ""
    private var dob= ""
    private var email= ""
    private var phoneCode= ""
    private var phoneNumber= ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingProfileEdit= ActivityProfieEditBinding.inflate(layoutInflater)
        setContentView(bindingProfileEdit.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        loadMyInfo()

        bindingProfileEdit.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        bindingProfileEdit.profileImagePickFab.setOnClickListener{
            imagePickDialog()
        }

        bindingProfileEdit.profileImagePickFab.setOnClickListener {
            imagePickDialog()
        }

        bindingProfileEdit.updateBtn.setOnClickListener {
            validateData()
        }

    }

    private fun validateData(){
        name = bindingProfileEdit.nameEdt.text.toString().trim()
        dob = bindingProfileEdit.dobEdt.text.toString().trim()
        email = bindingProfileEdit.emailEdt.text.toString().trim()
        phoneCode = bindingProfileEdit.countryCodePicker.selectedCountryCodeWithPlus
        phoneNumber = bindingProfileEdit.phoneNumberEdt.text.toString().trim()
        //validate data
        if (imageUri == null){
            //no image to upload to storage, just update db
            updateProfileDb(null.toString())
        }else{
            uploadProfileImageStorage()
        }
    }

    private fun uploadProfileImageStorage() {
        Log.d(TAG, "uploadProfileImageStorage: ")
        //show progress
        progressDialog.setMessage("Uploading user profile image")
        progressDialog.show()
        //setup img name and path
        val filePathAndName = "UserProfile/profile_${firebaseAuth.uid}"
        //Storage reference to upload image
        val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)
        ref.putFile(imageUri!!)
            .addOnProgressListener { snapshot ->
                //check image upload progress and show
                val progress = 100.0* snapshot.bytesTransferred / snapshot.totalByteCount
                Log.d(TAG, "uploadProfileImageStorage: progress: $progress")
                progressDialog.setMessage("Uploading profile image. Progress: $progress")

            }
            .addOnSuccessListener { taskSnapshot ->
                //image upload successfully, get url of uploaded image
                Log.d(TAG, "uploadProfileImageStorage: Image uploaded...")

                var uriTask = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);

                val uploadedImageUrl = uriTask.toString()
                if (uriTask.isSuccessful){
                    updateProfileDb(uploadedImageUrl)
                }

            }
            .addOnFailureListener{ e ->

                Log.e(TAG, "uploadProfileImageStorage: ", e)
                progressDialog.dismiss()
                Utils.toast(this,"Failed to upload due to ${e.message}")

            }
    }

    private fun updateProfileDb(uploadedImageUrl: String){
        Log.d(TAG, "updateProfileDb: uploadedImageUrl: $uploadedImageUrl")

        progressDialog.setMessage("Updating user info")
        progressDialog.show()

        val hashMap = HashMap<String,Any>()
        hashMap["name"] = "$name"
        hashMap["dob"] = "$dob"
        if(uploadedImageUrl != null){
            //update profileImageUrl in db only if uploaded image url is not null
            hashMap["profileImageUrl"]= "uploadedImageUrl"
        }


        if(myUserType.equals("Phone",true)){
            hashMap["email"] = "$email"
        }else if(myUserType.equals("Email",true) || myUserType.equals("Google",true)){
            hashMap["phoneCode"] = "$phoneCode"
            hashMap["phoneNumber"] = "$phoneNumber"

        }
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child("${firebaseAuth.uid}")
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateProfileDb: Updated...")
                progressDialog.dismiss()
                Utils.toast(this,"Updated...")
                imageUri = null
            }
            .addOnFailureListener {e ->
                Log.e(TAG, "updateProfileDb: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to update due to ${e.message}")
            }

    }

    private fun loadMyInfo(){
        Log.d(TAG, "loadMyInfo: ")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val dob = "${snapshot.child("dob").value}"
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    myUserType = "${snapshot.child("userType").value}"

                    val phone = phoneCode+phoneNumber

                    if (myUserType.equals("Email", true) || myUserType.equals("Google",true)){
                        bindingProfileEdit.emailTIL.isEnabled= false
                        bindingProfileEdit.emailEdt.isEnabled = false

                    }else{
                        bindingProfileEdit.phoneNumberTIL.isEnabled= false
                        bindingProfileEdit.phoneNumberEdt.isEnabled= false
                        bindingProfileEdit.countryCodePicker.isEnabled= false
                    }
                    //set data to UI
                    bindingProfileEdit.emailEdt.setText(email)
                    bindingProfileEdit.dobEdt.setText(dob)
                    bindingProfileEdit.nameEdt.setText(name)
                    bindingProfileEdit.phoneNumberEdt.setText(phoneNumber)

                    try {
                        //+84 --> 84
                        val phoneCodeInt = phoneCode.replace("+", "").toInt()
                        bindingProfileEdit.countryCodePicker.setCountryForPhoneCode(phoneCodeInt)
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                    //set profile img
                    try {
                        Glide.with(this@ProfieEditActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.icon_person_white)
                            .into(bindingProfileEdit.profileIv)
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun imagePickDialog(){
        val popupMenu = PopupMenu(this, bindingProfileEdit.profileImagePickFab)

        popupMenu.menu.add(Menu.NONE,1,1,"Camera")
        popupMenu.menu.add(Menu.NONE,2,2,"Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {item ->

            val itemId = item.itemId
            if(itemId == 1){

                //Check camera permission before take picture
                Log.d(TAG, "imagePickDialog: Camera Clicked, check if camera permission granted or not")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
                }else{
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }

            }else if(itemId == 2){

                //Check gallery permission before pick image
                Log.d(TAG, "imagePickDialog: Gallery Clicked, check if storage permission granted or not")
                //Tiramisu version or above don't need Storage permission to launch Gallery
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
                }else{
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }

            }

            return@setOnMenuItemClickListener true
        }
    }

    private val requestCameraPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){result ->
            Log.d(TAG, "requestCameraPermissions: result: $result")
            //check permissions granted or not
            var areAllGranted = true
            for(isGranted in result.values){
                areAllGranted = areAllGranted && isGranted
            }

            if (areAllGranted){
                Log.d(TAG, "requestCameraPermissions: All granted e.g. Camera, Storage")
                pickImageCamera()
            }else{
                Log.d(TAG, "requestCameraPermissions: All or either are denied")
                Utils.toast(this,"Camera or Storage or both permission denied")
            }

        }

    private val requestStoragePermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
            Log.d(TAG, "requestStoragePermissions: isGranted: $isGranted")
            //check permissions granted or not
            if (isGranted){
                pickImageGallery()
            }else{
                Utils.toast(this,"Storage permission denied")
            }

        }

    private fun pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image_title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image_description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            //Check img is captured or not
            if (result.resultCode == Activity.RESULT_OK){
                Log.d(TAG, "cameraActivityResultLauncher: Image captured: imageUri: $imageUri ")

                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.icon_person_white)
                        .into(bindingProfileEdit.profileIv)
                }catch (e:Exception){
                    Log.e(TAG, "cameraActivityResultLauncher: ", e)
                }
            }else{
                //Cancelled
                Utils.toast(this,"Cancelled!!")
            }

        }

    private fun pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
    }

    private val galleryActivityResultLauncher=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->

            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.icon_person_white)
                        .into(bindingProfileEdit.profileIv)
                }
                catch (e: java.lang.Exception){
                    Log.e(TAG, "galleryActivityResultLauncher: ", e)
                }
            }
        }

}