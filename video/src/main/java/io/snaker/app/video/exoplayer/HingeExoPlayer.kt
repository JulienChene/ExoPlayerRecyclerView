package io.snaker.app.exoplayerprototype

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import okhttp3.OkHttpClient

class HingeExoPlayer(val player: SimpleExoPlayer) {

    companion object {

        private const val MAX_CACHE_SIZE = (10 * 1024 * 1024).toLong()
        private const val MAX_FILE_SIZE = (5 * 1024 * 1024).toLong()
    }

    class Factory(private val context: Context,
                  private val okHttpClient: OkHttpClient,
                  private val filePath: String) {

        fun build(): HingeExoPlayer {
            val exoPlayer = ExoPlayerFactory.newSimpleInstance(context)
            val mediaSource = ExtractorMediaSource.Factory(CacheDataSourceFactory(context, okHttpClient, MAX_CACHE_SIZE, MAX_FILE_SIZE))
                .createMediaSource(Uri.parse(filePath))

            exoPlayer.prepare(mediaSource)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

            return HingeExoPlayer(exoPlayer)
        }
    }
}
