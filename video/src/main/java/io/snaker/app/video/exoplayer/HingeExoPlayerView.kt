package io.snaker.app.video.exoplayer

import android.content.Context
import android.util.AttributeSet
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import io.snaker.app.exoplayerprototype.HingeExoPlayer
import io.snaker.app.exoplayerprototype.VideoPlayer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import timber.log.Timber
import io.snaker.app.video.exoplayer.ExoPlayerExtensions.hasAudioTrack

class HingeExoPlayerView : PlayerView, VideoPlayer {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        useController = false
    }

    val

    override fun initializeAsync(filePath: String, okHttpClient: OkHttpClient) {
        if (player != null ) return

        val start = System.currentTimeMillis()
        val deferredPlayer = GlobalScope.async {
            HingeExoPlayer.Factory(context, okHttpClient, filePath).build().player
        }
        Timber.e("Initializing ExoPlayerExtensions took ${System.currentTimeMillis() - start}")

        runBlocking {
            player = deferredPlayer.await()
            resume()
        }
        Timber.e("Setting ExoPlayerExtensions took ${System.currentTimeMillis() - start}")
    }

    override fun initializeSync(filePath: String, okHttpClient: OkHttpClient) {
        if (player != null ) return

        val start = System.currentTimeMillis()
        player = HingeExoPlayer.Factory(context, okHttpClient, filePath).build().player
        resume()
        Timber.e("Initializing ExoPlayerExtensions took ${System.currentTimeMillis() - start}")
    }

    override fun resume() {
        (player as? SimpleExoPlayer)?.playWhenReady = true
    }

    override fun pause() {
        (player as? SimpleExoPlayer)?.playWhenReady = false
    }

    override fun release() {
        val exoPlayer = player as? SimpleExoPlayer ?: return
        exoPlayer.stop()
        exoPlayer.release()
    }

    override fun hasAudio() {
        val exoPlayer = player as? SimpleExoPlayer ?: return
        exoPlayer.hasAudioTrack()
    }
}
