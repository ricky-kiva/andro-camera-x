package com.dicoding.picodiploma.mycamera

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.mycamera.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currentPhotoPath: String

    // launcher for CameraX
    private val launcherIntentCameraX = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // get the picture from parsed extra as File
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra("picture")
            } as? File // use this type of casting so it will be null if the result failed to be grasped

            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            myFile?.let { file ->
                // calls function from custom Utils class
                rotateAndCompressFile(file, isBackCamera)
                // set the View
                binding.previewImageView.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    // launcher for default Camera (Intent Camera)
    private val launcherIntentCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            /* Not using this anymore, because we use Full Intent Camera (not thumbnail)
            val imageBitmap = it.data?.extras?.get("data") as Bitmap
            binding.previewImageView.setImageBitmap(imageBitmap)*/

            val myFile = File(currentPhotoPath)
            myFile.let { file ->
                // set the View
                binding.previewImageView.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // request permission onCreate
        if (!allPermissionGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        binding.cameraXButton.setOnClickListener { startCameraX() }
        binding.cameraButton.setOnClickListener { startTakePhoto() }
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.uploadButton.setOnClickListener { uploadImage() }
    }

    private fun uploadImage() {
        Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show()
    }

    private fun startGallery() {
        Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show()
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // checks whether there is activity that can handle given intent (IMAGE_CAPTURE)
        // packageManager is Android service that give information about installed app
        intent.resolveActivity(packageManager)

        createTempFile(application).also {
            // create Uri for the temporary file
            val photoURI: Uri = FileProvider.getUriForFile(this@MainActivity, "com.dicoding.picodiploma.mycamera", it)
            // get absolute path of the photoUri
            currentPhotoPath = it.absolutePath
            // set EXTRA_OUTPUT to the Uri of the temporary file
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            // launch the intent via launcherIntentCamera variable
            launcherIntentCamera.launch(intent)
        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // checks the requestCode given by ActivityCompat.requestPermission()
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionGranted()) {
                Toast.makeText(this, "Insufficient Permission", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // this returns boolean whether if all permission inside REQUIRED_PERMISSIONS is given
    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

}