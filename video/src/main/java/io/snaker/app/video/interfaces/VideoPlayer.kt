package io.snaker.app.video.interfaces

import com.google.android.exoplayer2.ui.PlayerView
import io.snaker.app.video.media.PlaybackInfo

interface VideoPlayer {
    fun initialize()
    fun isInitialized(): Boolean
    fun isPlaying(): Boolean
    fun resume()
    fun pause()
    fun stop()
    fun hasAudio()
    fun getPlayerView(): PlayerView
    fun getPlayerOrder(): Int
    fun getPlaybackInfo(): PlaybackInfo
    fun wantsToPlay(): Boolean
}
