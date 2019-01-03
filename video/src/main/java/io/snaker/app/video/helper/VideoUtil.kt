package io.snaker.app.video.helper

import android.graphics.Point
import android.graphics.Rect
import androidx.annotation.FloatRange
import android.view.ViewParent
import io.snaker.app.video.interfaces.VideoPlayer
import io.snaker.app.video.player.VideoRecyclerView

object VideoUtil {

    val LIB_NAME = "hinge/video"

    /**
     * Get the ratio in range of 0.0 ~ 1.0 the visible area of a [VideoPlayer]'s playerView.
     *
     * @param player the [VideoPlayer] need to investigate.
     * @param parent the [ViewParent] that holds the [VideoPlayer]. If `null` or
     * not a [VideoRecyclerView] then this method must returns 0.0f;
     * @return the value in range of 0.0 ~ 1.0 of the visible area.
     */
    @FloatRange(from = 0.0, to = 1.0)
    fun visibleAreaOffset(player: VideoPlayer, parent: ViewParent?): Float {
        if (parent == null || parent !is VideoRecyclerView) return 0.0f

        val playerView = player.getPlayerView()
        val drawRect = Rect()
        playerView.getDrawingRect(drawRect)
        val drawArea = drawRect.width() * drawRect.height()

        val playerRect = Rect()
        val visible = playerView.getGlobalVisibleRect(playerRect, Point())

        var offset = 0f
        if (visible && drawArea > 0) {
            val visibleArea = playerRect.height() * playerRect.width()
            offset = visibleArea / drawArea.toFloat()
        }
        return offset
    }
}
