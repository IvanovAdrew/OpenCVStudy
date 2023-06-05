package com.example.opencv_study

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.recyclerview.widget.RecyclerView

class ImageChecking : AppCompatActivity() {
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_cheking)
        recyclerView = findViewById(R.id.recyclerView)
        imageAdapter = ImageAdapter(getImagesFromInternalStorage())
        recyclerView.adapter = imageAdapter
    }

    private fun getImagesFromInternalStorage(): List<ImageModel> {
        val images = mutableListOf<ImageModel>()
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val files = directory?.listFiles()

        files?.forEach { file ->
            if (file.isFile && (file.path.endsWith(".jpg") || file.path.endsWith(".png"))) {
                val uri = Uri.fromFile(file)
                images.add(ImageModel(uri))
            }
        }
        return images
    }
}



