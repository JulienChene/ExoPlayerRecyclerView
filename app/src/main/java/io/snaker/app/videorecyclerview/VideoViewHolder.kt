package io.snaker.app.videorecyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ui.PlayerView
import io.snaker.app.video.exoplayer.HingeExoPlayerView
import io.snaker.app.video.helper.VideoUtil
import io.snaker.app.video.interfaces.VideoPlayer
import io.snaker.app.video.media.PlaybackInfo
import okhttp3.OkHttpClient

class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view), VideoPlayer {

    companion object {
        private const val VISIBLE_VIEW_OFFSET_TO_PLAY = 0.70f
    }

    private val exoPlayerView: HingeExoPlayerView = itemView.findViewById(R.id.video)

    private var filePath = ""
    private var okHttpClient: OkHttpClient? = null

    fun bind(media: Media, okHttpClient: OkHttpClient) {
        (exoPlayerView as? HingeExoPlayerView)?.initializeSync(media.url, okHttpClient)
        (exoPlayerView as? HingeExoPlayerView)?.resume()
    }

    override fun initialize() {
        if (isInitialized()) return

        val videoPath = filePath
        val httpClient = okHttpClient

        if (videoPath.isNotBlank() && httpClient != null) {
            exoPlayerView.initializeSync(videoPath, httpClient)
        }
    }

    override fun isInitialized(): Boolean =
        exoPlayerView.player != null

    override fun isPlaying(): Boolean {
        return exoPlayerView.isPlaying()
    }

    override fun resume() {
        exoPlayerView.resume()
    }

    override fun pause() {
        exoPlayerView.pause()
    }

    override fun stop() {
        exoPlayerView.release()
    }

    override fun hasAudio() {
        exoPlayerView.hasAudio()
    }

    override fun getPlayerView(): PlayerView {
        return exoPlayerView
    }

    override fun getPlayerOrder(): Int = adapterPosition

    override fun getPlaybackInfo(): PlaybackInfo {
        return exoPlayerView.getPlaybackInfo()
    }

    override fun wantsToPlay(): Boolean {
        val visibleAreaOffset = VideoUtil.visibleAreaOffset(this, itemView.parent)

        return visibleAreaOffset >= VISIBLE_VIEW_OFFSET_TO_PLAY
    }

}
