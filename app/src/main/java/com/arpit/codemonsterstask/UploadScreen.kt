package com.arpit.codemonsterstask

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_upload_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.jar.Manifest

class UploadScreen : AppCompatActivity() {

    var currentFile: Uri? = null
    var imageRef = Firebase.storage.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_screen)

        buGallery.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, 0)
            }
        }
        buUpload.setOnClickListener {
            uploadImage("myImage")
        }

        buCamera.isEnabled = false
        if(ActivityCompat.checkSelfPermission(this , android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this , arrayOf(android.Manifest.permission.CAMERA) , 111)
        } else{
            buCamera.isEnabled = true
        }

        if (ActivityCompat.checkSelfPermission(this , android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this , arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE) , 666)
        }

        buCamera.setOnClickListener {
            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent , 101)
        }
        listFiles()
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            buCamera.isEnabled = true
        }
        if(requestCode == 666 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            
        }
    }

    private fun uploadImage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            currentFile?.let {
                imageRef.child("images/$filename").putFile(it).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@UploadScreen,
                        "Image Uploaded Successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@UploadScreen, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun listFiles() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val images = imageRef.child("images/").listAll().await()
            val imageUrls = mutableListOf<String>()
            for (image in  images.items){
                val url = image.downloadUrl.await()
                imageUrls.add(url.toString())
            }
            withContext(Dispatchers.Main) {
                val imageAdapter = ImageAdapter(imageUrls)
                rvImages.apply {
                    adapter = imageAdapter
                    layoutManager = LinearLayoutManager(this@UploadScreen)
                    Toast.makeText(this@UploadScreen, "Images Loaded", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@UploadScreen, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == 0){
            data?.data?.let {
                currentFile = it
                ivImage.setImageURI(it)
            }
        }
        if (requestCode == 101){
            var  pic =data!!.getParcelableExtra<Bitmap>("data")
            ivImage.setImageBitmap(pic)
            data.data.let {
                currentFile = it
                uploadImage(it.toString())
            }
        }
    }
}