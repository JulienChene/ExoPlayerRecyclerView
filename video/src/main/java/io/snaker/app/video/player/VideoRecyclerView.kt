package io.snaker.app.video.player

import android.content.Context
import android.os.PowerManager
import android.util.AttributeSet
import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.snaker.app.video.helper.Common
import io.snaker.app.video.helper.PlayerManager
import io.snaker.app.video.interfaces.VideoPlayer
import io.snaker.app.video.interfaces.VideoPlayerHolder
import timber.log.Timber

class VideoRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private val playerManager = PlayerManager()

    @CallSuper
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

//        if (adapter != null) dataObserver.registerAdapter(adapter)

//        if (animatorFinishHandler == null)
//            animatorFinishHandler = Handler(AnimatorHelper(this))

//        setCacheManager(CacheManager.DEFAULT)

//        screenState = if (isScreenOn()) {
//            View.SCREEN_STATE_ON
//        } else {
//            View.SCREEN_STATE_OFF
//        }
    }

    private fun isScreenOn(): Boolean {
        return (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isInteractive == true
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

//        animatorFinishHandler?.removeCallbacksAndMessages(null)
//        animatorFinishHandler = null
//
//        setCacheManager(null)

        // Release all players
        playerManager.copyPlayers()
            .map { videoPlayer ->
                if (videoPlayer.isPlaying())
                    playerManager.pause(videoPlayer)
                playerManager.release(videoPlayer)
                playerManager.detachPlayer(videoPlayer)
            }
        playerManager.clear()

//        dataObserver.registerAdapter(null)
    }

    /**
     * Starts playing videos when ViewHolder attached to Window
     */
    @CallSuper
    override fun onChildAttachedToWindow(child: View) {
        super.onChildAttachedToWindow(child)
        val holder = getChildViewHolder(child)
        if (holder == null || holder !is VideoPlayer) return

        val videoPlayer = holder as VideoPlayer
        Timber.e("OnChildAttachedToWindow: $videoPlayer")

        if (playerManager.manages(videoPlayer)) {
            if (scrollState == SCROLL_STATE_IDLE && !videoPlayer.isPlaying()) {
                playerManager.play(videoPlayer)
            }
        } else {
            if (playerManager.attachPlayer(videoPlayer)) {
//                            val playbackInfo = getPlaybackInfo(videoPlayer.getPlayerOrder())
//                            playerManager.initialize(videoPlayer, playbackInfo)
                playerManager.initialize(videoPlayer, null)
//                            dispatchUpdateOnAnimationFinished(false)
            }
        }
    }

    /**
     * Pauses [VideoPlayer]s when ViewHolder detached from window
     */
    @CallSuper
    override fun onChildDetachedFromWindow(child: View) {
        super.onChildDetachedFromWindow(child)

        val holder = getChildViewHolder(child)
        if (holder == null || holder !is VideoPlayer) return
        val videoPlayer = holder as VideoPlayer

        val isPlayerManaged = playerManager.manages(videoPlayer)
        if (videoPlayer.isPlaying()) {
            if (!isPlayerManaged) {
                throw IllegalStateException("Player is playing while it is not in managed state: " + videoPlayer)
            }

//            savePlaybackInfo(videoPlayer.getPlayerOrder(), videoPlayer.getCurrentPlaybackInfo())
            playerManager.pause(videoPlayer)
        }

        playerManager.detachPlayer(videoPlayer)
        Timber.e("detach $videoPlayer")

//        dispatchUpdateOnAnimationFinished(true)

//        videoPlayer.stop()
    }

    @CallSuper
    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        if (childCount == 0) return

        // 1. Find players that are not qualified to play anymore.
        playerManager.copyPlayers()
            .filterNot { player -> Common.canPlay(player) }
            .filter { player -> player.isPlaying() }
            .map { player ->
//                savePlaybackInfo(player.getPlayerOrder(), player.getCurrentPlaybackInfo())
                playerManager.pause(player)
            }

        // 2. Refresh the good players list
        var firstVisiblePosition = NO_POSITION
        var lastVisiblePosition = NO_POSITION

        if (layoutManager is LinearLayoutManager) {
            firstVisiblePosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            lastVisiblePosition = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        }

        if (firstVisiblePosition <= lastVisiblePosition
            && (firstVisiblePosition != NO_POSITION || lastVisiblePosition != NO_POSITION)) {
            for (position in firstVisiblePosition..lastVisiblePosition) {
                val holder = findViewHolderForAdapterPosition(position)
                val videoPlayer = (holder as? VideoPlayerHolder)?.get() ?: continue
                if (Common.canPlay(videoPlayer)) {
                    if (!playerManager.manages(videoPlayer)) {
                        playerManager.attachPlayer(videoPlayer)
                    }
                    if (!videoPlayer.isPlaying()) {
                        // We are passing the PlaybackInfo stored in cache because we are releasing
                        // ExoPlayer when view gets detached, so PlaybackInfo gets reset for the
                        // new ExoPlayer. If the player wasn't released, we make a faster call to
                        // videoPlayer.getCurrentPlaybackInfo().
//                        val info = when (videoPlayer.isInitialized()) {
//                            true -> videoPlayer.getCurrentPlaybackInfo()
//                            else -> this.getPlaybackInfo(videoPlayer.getPlayerOrder())
//                        }
                        playerManager.initialize(videoPlayer, null)
                    }
                }
            }
        }
        // Play or Pause players if they're showing up
        val playerList = playerManager.copyPlayers()
        if (playerList.isEmpty()) return // No available player, return.

        val candidates = playerList.filter { it.wantsToPlay() }
        candidates.sortedWith(Common.ORDER_COMPARATOR)

        val shouldPickLast = dy >= 0

        val toPlay = when {
            shouldPickLast -> candidates.lastOrNull()
            else -> candidates.firstOrNull()
        }
        toPlay?.resume()

        playerList.remove(toPlay)

        // Pause players that should't play
        playerList.map { videoPlayer ->
            if (videoPlayer.isPlaying()) {
//                savePlaybackInfo(videoPlayer.getPlayerOrder(), videoPlayer.getCurrentPlaybackInfo())
                playerManager.pause(videoPlayer)
            }
        }
    }
}
