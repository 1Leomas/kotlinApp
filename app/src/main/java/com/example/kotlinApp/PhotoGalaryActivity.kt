package com.example.kotlinApp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import java.io.File
import com.example.kotlinApp.databinding.ActivityCameraBinding


class PhotoGalaryActivity : AppCompatActivity() {

    private lateinit var cameraBinding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_galary)

        cameraBinding = ActivityCameraBinding.inflate(layoutInflater)

        val data = intent.getStringArrayListExtra(EXTRA_MESSAGE)

        val imagePathString = data?.get(0)

        val cameraState = data?.get(1)

        val photoView = findViewById<ImageView>(R.id.photoView)

        val imgBitmap = BitmapFactory.decodeFile(imagePathString)

        val matrix = Matrix()

        if(cameraState == "1") matrix.postRotate(90f)
        else matrix.postRotate(270f)

        val rotatedBitMap = Bitmap.createBitmap(
            imgBitmap, 0, 0, imgBitmap.width, imgBitmap.height, matrix, false
        )

        photoView.setImageBitmap(rotatedBitMap)


        if (imagePathString != null) {
            Log.e("IMAGE PATH", imagePathString)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        for (file in getOutputDirectory().listFiles()) {
            if (!file.isDirectory) {
                file.delete()
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }
}