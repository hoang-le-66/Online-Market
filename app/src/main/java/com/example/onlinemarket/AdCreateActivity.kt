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
import android.view.Display.Mode
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.example.onlinemarket.databinding.ActivityAdCreateBinding
import com.google.api.LogDescriptor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlin.math.log

class AdCreateActivity : AppCompatActivity() {
    private lateinit var bindingAdCreate: ActivityAdCreateBinding

    private companion object{
        private const val TAG = "ADD_CREATE_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth
    private var imageUri: Uri? = null

    private lateinit var imagePickedArrayList: ArrayList<ModelPickedImage>
    //Adapter to be set in RecyclerView that will load list of images
    private lateinit var adapterPickedImage: AdapterPickedImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingAdCreate= ActivityAdCreateBinding.inflate(layoutInflater)
        setContentView(bindingAdCreate.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        //Setup and set the categories adapter to the Category Input Filed i.e categoryAct
        val adapterCategories = ArrayAdapter(this,R.layout.row_category_act, Utils.categories)
        bindingAdCreate.categoryAct.setAdapter(adapterCategories)

        //Setup and set the conditions adapter to the Condition Input Filed i.e conditionAct
        val adapterConditions = ArrayAdapter(this, R.layout.row_condition_act, Utils.categories)
        bindingAdCreate.categoryAct.setAdapter(adapterCategories)

        //init imagePickedArrayList
        imagePickedArrayList= ArrayList()

        //loadImages
        loadImages()

        bindingAdCreate.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        bindingAdCreate.toolBarAdImageBtn.setOnClickListener {
            showImagePickOptions()
        }

        bindingAdCreate.postAdBtn.setOnClickListener {
            validateData()
        }
    }


    private fun loadImages() {
        Log.d(TAG, "loadImages: ")
        adapterPickedImage = AdapterPickedImage(this, imagePickedArrayList)

        bindingAdCreate.imagesRv.adapter = adapterPickedImage
    }

    private fun showImagePickOptions() {
        Log.d(TAG, "showImagePickOptions: ")
        //init PopupMenu
        val popupMenu = PopupMenu(this, bindingAdCreate.toolBarAdImageBtn)
        /*add menu item to popup menu
        * param#1: GroupID, #2: ItemID, #3 OrderID, #4 Menu Title*/
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")

        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {item ->
            val itemId = item.itemId
            if (itemId == 1){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    val cameraPermissions = arrayOf(android.Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermissions)
                }else{
                    val cameraPermissions = arrayOf(android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }

            }else if (itemId == 2){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery()
                }else{
                    val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermission)
                }
            }
            true
        }
    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){  isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted: $isGranted")

        if (isGranted){
            pickImageGallery()

        }else{
            Utils.toast(this,"Storage permission denied")
        }

    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){result ->
        Log.d(TAG, "requestCameraPermission: result: $result")
        var areAllGranted = true
        for (isGranted in result.values){
            areAllGranted = areAllGranted && isGranted
        }

        if (areAllGranted){
            pickImageCamera()
        }else{
            Utils.toast(this, "Camera or storage or both permissions are denied")
        }
    }

    private fun pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ")
        //Setup Content values, MediaStore to capture high quality img using camera intent
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMPT_IMAGE_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMPT_IMAGE_DESCRIPTION")
        //Uri of the image to be captured from camera
        imageUri= contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        //Intent to launch camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)

    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        Log.d(TAG, "galleryActivityResultLauncher: ")
        //Check pick or not
        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageUri = data!!.data
            Log.d(TAG, "galleryActivityResultLauncher: imageUri: $imageUri")

            val timestamp = "${Utils.getTimeStamp()}"
            val modelPickedImage = ModelPickedImage(timestamp, imageUri, null, false)

            imagePickedArrayList.add(modelPickedImage)
            loadImages()
        }else{
            Utils.toast(this,"Cancelled...!")
        }

    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        Log.d(TAG, "cameraActivityResultLauncher: ")
        if (result.resultCode ==Activity.RESULT_OK){
            Log.d(TAG, "cameraActivityResultLauncher: imageUri $imageUri")

            val timestamp = "${Utils.getTimeStamp()}"
            val modelPickedImage = ModelPickedImage(timestamp, imageUri, null,false)

            imagePickedArrayList.add(modelPickedImage)
            //reload img
            loadImages()

        }else{
            Utils.toast(this,"Cancelled...!")
        }

    }

    private var brand = ""
    private var category = ""
    private var condition = ""
    private var address = ""
    private var price = ""
    private var title = ""
    private var description = ""
    private var latitude = 0.0
    private var longitude = 0.0

    private fun validateData(){
        Log.d(TAG, "validateData: ")

        brand = bindingAdCreate.brandEdt.text.toString().trim()
        category = bindingAdCreate.categoryAct.text.toString().trim()
        condition = bindingAdCreate.conditionAct.text.toString().trim()
        address = bindingAdCreate.locationAct.text.toString().trim()
        price = bindingAdCreate.priceEdt.text.toString().trim()
        title = bindingAdCreate.titleEdt.text.toString().trim()
        description = bindingAdCreate.descriptionEdt.text.toString().trim()

        if (brand.isEmpty()){
            bindingAdCreate.brandEdt.error = "Enter Brand"
            bindingAdCreate.brandEdt.requestFocus()
        }else if (category.isEmpty()){
            bindingAdCreate.categoryAct.error = "Choose Category"
            bindingAdCreate.categoryAct.requestFocus()

        }else if (condition.isEmpty()){
            bindingAdCreate.conditionAct.error = "Choose Condition"
            bindingAdCreate.conditionAct.requestFocus()

        }else if(title.isEmpty()){
            bindingAdCreate.titleEdt.error = "Enter Title"
            bindingAdCreate.titleEdt.requestFocus()

        }else if (description.isEmpty()){
            bindingAdCreate.descriptionEdt.error= "Enter Description"
            bindingAdCreate.descriptionEdt.requestFocus()

        }else{
            postAd()
        }
    }

    private fun postAd(){
        Log.d(TAG, "postAd: ")

        progressDialog.setMessage("Publishing Ad")
        progressDialog.show()
        //get current timestamp
        val timestamp = Utils.getTimeStamp()
        val refAds = FirebaseDatabase.getInstance().getReference("Ads")
        val keyId = refAds.push().key

        //setup data to add
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$keyId"
        hashMap["uid"] = "$keyId"
        hashMap["category"] = "$keyId"
        hashMap["condition"] = "$keyId"
        hashMap["brand"] = "$keyId"
        hashMap["address"] = "$keyId"
        hashMap["price"] = "$keyId"
        hashMap["title"] = "$keyId"
        hashMap["description"] = "$keyId"
        hashMap["status"] = "$keyId"
        hashMap["timestamp"] = "$keyId"
        hashMap["latitude"] = latitude
        hashMap["longitude"] = longitude

        //set data to Db. Ads -> AdId -> AdDataJSON
        refAds.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "postAd: Ad Published")
                uploadImagesStorage(keyId)
            }
            .addOnFailureListener {e ->
                Log.e(TAG, "postAd: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed due to ${e.message}")
            }
    }

    private fun uploadImagesStorage(adId: String){
        for (i in imagePickedArrayList.indices){
            val modelPickedImage = imagePickedArrayList[i]
            val imageName = modelPickedImage.id
            val filePathAndName = "Ads/$imageName"
            val imageIndexForProgress = i + 1

            val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
            storageReference.putFile(modelPickedImage.imageUri!!)
                .addOnProgressListener {snapshot ->
                    val progress = 100.0*snapshot.bytesTransferred / snapshot.totalByteCount
                    Log.d(TAG, "uploadImagesStorage: progress: $progress")

                    val message = "Uploading $imageIndexForProgress of ${imagePickedArrayList.size} images... Progress ${progress.toInt()}"
                    Log.d(TAG, "uploadImagesStorage: message: $message")

                    progressDialog.setMessage(message)
                    progressDialog.show()

                }
                .addOnSuccessListener {taskSnapShot ->
                    Log.d(TAG, "uploadImagesStorage: onSuccess")

                    val uriTask = taskSnapShot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val uploadedImageUrl = uriTask.result

                    if (uriTask.isSuccessful){
                        val hashMap = HashMap<String, Any>()
                        hashMap["id"] = "${modelPickedImage.id}"
                        hashMap["imageUrl"] = "$uploadedImageUrl"

                        val ref = FirebaseDatabase.getInstance().getReference("Ads")
                        ref.child(adId).child("Images")
                            .child(imageName)
                            .updateChildren(hashMap)

                    }

                    progressDialog.dismiss()

                }
                .addOnFailureListener{e ->
                    Log.e(TAG, "uploadImagesStorage: ", e)
                    progressDialog.dismiss()

                }
        }
    }

}