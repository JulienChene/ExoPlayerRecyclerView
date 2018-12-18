package io.snaker.app.videorecyclerview

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager

class PhotoViewHolder(private val requestManager: RequestManager, view: View) : RecyclerView.ViewHolder(view) {

    private val imageView: ImageView = itemView.findViewById(R.id.image)

    fun bind(media: Media) {
        requestManager
            .load(media.url)
            .into(imageView)
    }

}
