package com.example.opencv_study

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import com.bumptech.glide.Glide

class ImageAdapter(private val imageList: List<ImageModel>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = imageList[position]
        //holder.imageView.setImageURI(image.uri)
        val context = holder.itemView.context
        context.imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(image.uri)
                .target(holder.imageView)
                .build()
        )

        // UsingGlide to load image
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
