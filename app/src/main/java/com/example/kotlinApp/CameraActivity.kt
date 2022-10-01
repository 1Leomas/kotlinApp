package com.example.kotlinApp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.kotlinApp.databinding.ActivityCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

const val EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"

@SuppressLint("RestrictedApi")
class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private var imagePreview: Preview? = null

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture? = null

    private lateinit var outputDirectory: File

    private var cameraControl: CameraControl? = null

    private var cameraInfo: CameraInfo? = null

    private var lastImagePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        outputDirectory = getOutputDirectory()

        binding.imageCaptureButton.setOnClickListener {
            takePicture()
        }

        binding.galaryButton.setOnClickListener{
            if(lastImagePath.length > 1){
                val intent = Intent(this, PhotoGalaryActivity::class.java).apply {
                    putExtra(EXTRA_MESSAGE, lastImagePath)
                }
                lastImagePath = ""
                startActivity(intent)
            } else{
                Toast.makeText(this@CameraActivity, "First take a photo", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProviderFuture.addListener({
            imagePreview = Preview.Builder().apply {
                setTargetAspectRatio(AspectRatio.RATIO_16_9)
                setTargetRotation(binding.previewView.display.rotation)
            }.build()

            imageCapture = ImageCapture.Builder().apply {
                setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            }.build()

            videoCapture = VideoCapture.Builder().apply {
                setTargetAspectRatio(AspectRatio.RATIO_16_9)
            }.build()

            val cameraProvider = cameraProviderFuture.get()
            val camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                imagePreview,
                imageCapture,
                videoCapture
            )
            binding.previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            imagePreview?.setSurfaceProvider(binding.previewView.surfaceProvider)
            cameraControl = camera.cameraControl
            cameraInfo = camera.cameraInfo
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePicture() {
        val file = createFile(
            outputDirectory,
            FILENAME,
            PHOTO_EXTENSION
        )
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture?.takePicture(outputFileOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val msg = "Photo capture succeeded: ${file.absolutePath}"
                binding.previewView.post {
                    Toast.makeText(this@CameraActivity, msg, Toast.LENGTH_LONG).show()
                }

                lastImagePath = file.absolutePath
            }

            override fun onError(exception: ImageCaptureException) {
                val msg = "Photo capture failed: ${exception.message}"
                binding.previewView.post {
                    Toast.makeText(this@CameraActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    companion object {
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

        fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )
    }
}