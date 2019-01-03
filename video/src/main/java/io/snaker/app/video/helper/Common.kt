package io.snaker.app.video.helper

import android.graphics.Point
import android.graphics.Rect
import androidx.annotation.RestrictTo
import io.snaker.app.video.interfaces.VideoPlayer
import java.util.*

/**
 * A hub for internal convenient methods.
 */

internal object Common {

    private fun compare(x: Int, y: Int): Int = if (x < y) -1 else if (x == y) 0 else 1

    @RestrictTo(RestrictTo.Scope.LIBRARY) //
    var ORDER_COMPARATOR: Comparator<VideoPlayer> = Comparator { o1, o2 -> Common.compare(o1.getPlayerOrder(), o2.getPlayerOrder()) }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun canPlay(videoPlayer: VideoPlayer): Boolean = videoPlayer.getPlayerView().getGlobalVisibleRect(Rect(), Point())
}
