package com.dicoding.picodiploma.mycamera

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.dicoding.picodiploma.mycamera.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private var imageCapture: ImageCapture? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.captureImage.setOnClickListener { takePhoto() }
        binding.switchCamera.setOnClickListener {
            cameraSelector = if (cameraSelector.equals(CameraSelector.DEFAULT_BACK_CAMERA)) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA

            startCamera()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUI()
        startCamera()
    }

    private fun takePhoto() {
        // val variable = nullableValue ?: defaultValue
       val imageCapture = imageCapture ?: return

        // calls function from custom Utils class
        // make file to store captured photo
        val photoFile = createFile(application)

        // make builder to configure output options of captured image
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // method to take picture
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                // this when image is saved
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@CameraActivity, "Image captured!", Toast.LENGTH_SHORT).show()
                    val intent = Intent()
                    // send the photoFile
                    intent.putExtra("picture", photoFile)
                    // send boolean extra whether it's from back camera or not
                    intent.putExtra("isBackCamera", cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                    // set the intent result on MainActivity
                    setResult(MainActivity.CAMERA_X_RESULT, intent)
                    finish()
                }

                // this when there is error
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun startCamera() {
        // bind camera lifecycle to lifecycle owners
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        // sets listener when ProcessCameraProvider is available
        cameraProviderFuture.addListener({
            // get ProcessCameraProvider that has been on bind
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // build live camera preview as this UseCase
            val preview = Preview.Builder()
                .build()
                .also {
                    // set surface preview for the preview
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // to capture still image from camera
            imageCapture = ImageCapture.Builder().build()

            try {
                // ensure camera not being used by another UseCase by unbinding previous bound
                cameraProvider.unbindAll()
                // UseCases to this camera & starts camera preview
                // the 3th parameter is UseCase for displaying live camera preview
                // the 4th parameter is UseCase for capturing still images
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Toast.makeText(this@CameraActivity, "Failed to launch camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this)) // execute cameraProviderFuture on main thread
    }

    // hide systemUI overlay
    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}