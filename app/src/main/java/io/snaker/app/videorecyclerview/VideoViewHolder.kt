package io.snaker.app.videorecyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.snaker.app.video.exoplayer.HingeExoPlayerView
import okhttp3.OkHttpClient

class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val exoPlayerView: View = itemView.findViewById(R.id.video)

    fun bind(media: Media, okHttpClient: OkHttpClient) {
        (exoPlayerView as? HingeExoPlayerView)?.initializeSync(media.url, okHttpClient)
        (exoPlayerView as? HingeExoPlayerView)?.resume()
    }

}
