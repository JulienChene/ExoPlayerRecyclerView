package io.snaker.app.videorecyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import okhttp3.OkHttpClient

class VideoAdapter(private val medias: List<Media>,
                   private val requestManager: RequestManager,
                   private val okHttpClient: OkHttpClient) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class Type(val value: Int) {
        PhotoVH(0),
        VideoVH(1);

        companion object {
            fun fromId(other: Int) =
                values().firstOrNull { it.value == other } ?: PhotoVH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val type = Type.fromId(viewType)
        val resId = when (type) {
            Type.PhotoVH -> R.layout.photo_view_holder
            Type.VideoVH -> R.layout.video_view_holder
        }

        val view = LayoutInflater.from(parent.context).inflate(resId, parent, false)
        return when (type) {
            Type.PhotoVH -> PhotoViewHolder(requestManager, view)
            Type.VideoVH -> VideoViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return medias.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (medias[position].isPhoto) {
            true -> Type.PhotoVH.value
            else -> Type.VideoVH.value
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val media = medias[position]
        val viewType = getItemViewType(position)
        when (Type.fromId(viewType)) {
            Type.PhotoVH -> (holder as PhotoViewHolder).bind(media)
            Type.VideoVH -> (holder as VideoViewHolder).bind(media, okHttpClient)
        }
    }
}
