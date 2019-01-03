package io.snaker.app.video.helper

import io.snaker.app.video.interfaces.VideoPlayer
import io.snaker.app.video.media.PlaybackInfo
import java.util.*

/**
 * Manage the collection of [VideoPlayer]s for a specific [VideoRecyclerView].
 *
 * Task: collect all Players in which "[Common.canPlay]"
 * returns true, then initialize them.
 */

internal class PlayerManager {

    // Make sure each VideoPlayer will present only once in this Manager.
    private val players = HashSet<VideoPlayer>()

    fun attachPlayer(player: VideoPlayer): Boolean {
        return players.add(player)
    }

    fun detachPlayer(player: VideoPlayer): Boolean {
        return players.remove(player)
    }

    fun manages(player: VideoPlayer): Boolean {
        return players.contains(player)
    }

    /**
     * Return a "Copy" of the collection of players this manager is managing.
     *
     * @return a non null collection of Players that are managed.
     */
    fun copyPlayers(): ArrayList<VideoPlayer> {
        return ArrayList(this.players)
    }

    fun initialize(player: VideoPlayer,
                   playbackInfo: PlaybackInfo?) {
//        player.initialize(playbackInfo)
        player.initialize()
    }

    fun play(player: VideoPlayer) {
        player.resume()
    }

    fun pause(player: VideoPlayer) {
        player.pause()
    }

    fun release(player: VideoPlayer) {
        player.stop()
    }

    fun clear() {
        this.players.clear()
    }
}
