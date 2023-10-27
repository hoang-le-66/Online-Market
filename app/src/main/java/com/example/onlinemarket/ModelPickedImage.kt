package com.example.onlinemarket

import android.net.Uri
//this model class will be used to show images
class ModelPickedImage {
    var id = ""
    var imageUri: Uri? = null
    var imageUrl: String?= null
    var fromInternet = false
    //Empty constructor require for Db
    constructor()

    constructor(id: String, imageUri: Uri?, imageUrl: String?, fromInternet: Boolean) {
        this.id = id
        this.imageUri = imageUri
        this.imageUrl = imageUrl
        this.fromInternet = fromInternet
    }

}