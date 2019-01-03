package io.snaker.app.video.exoplayer

import android.content.Context
import android.util.AttributeSet
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import io.snaker.app.exoplayerprototype.HingeExoPlayer
import okhttp3.OkHttpClient
import io.snaker.app.video.exoplayer.ExoPlayerExtensions.hasAudioTrack
import io.snaker.app.video.media.PlaybackInfo

class HingeExoPlayerView : PlayerView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        useController = false
    }

    private val playbackInfo = PlaybackInfo()

    fun initializeSync(filePath: String, okHttpClient: OkHttpClient) {
        if (player != null ) return

        player = HingeExoPlayer.Factory(context, okHttpClient, filePath).build().player
        resume()
    }

    fun resume() {
        getSimpleExoPlayer()?.playWhenReady = true
    }

    fun pause() {
        getSimpleExoPlayer()?.playWhenReady = false
    }

    fun release() {
        val exoPlayer = getSimpleExoPlayer() ?: return
        exoPlayer.stop()
    }

    fun hasAudio() {
        val exoPlayer = getSimpleExoPlayer() ?: return
        exoPlayer.hasAudioTrack()
    }

    fun isPlaying(): Boolean {
        return getSimpleExoPlayer()?.playWhenReady == true
    }

    fun getPlaybackInfo(): PlaybackInfo {
        val player = getSimpleExoPlayer() ?: return PlaybackInfo()

        playbackInfo.resumeWindow = player.currentWindowIndex
        playbackInfo.resumeVolume = player.volume
        playbackInfo.resumePosition = when {
            player.isCurrentWindowSeekable -> Math.max(0, player.currentPosition)
            else -> C.TIME_UNSET
        }

        return playbackInfo
    }

    private fun getSimpleExoPlayer(): SimpleExoPlayer? =
        player as? SimpleExoPlayer
}
