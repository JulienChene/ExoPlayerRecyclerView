package io.snaker.app.video.exoplayer

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format

object ExoPlayerExtensions {

    fun ExoPlayer.hasAudioTrack(): Boolean {
        val trackGroups = currentTrackGroups

        for (trackGroupsPos in 0 until trackGroups.length) {
            val trackGroup = trackGroups[trackGroupsPos]
            for (trackGroupPos in 0 until trackGroup.length) {
                val format = trackGroup.getFormat(trackGroupPos)
                if (format.channelCount != Format.NO_VALUE) return true
            }
        }

        return false
    }
}
