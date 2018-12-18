package io.snaker.app.exoplayerprototype

import okhttp3.OkHttpClient

interface VideoPlayer {
    fun initializeSync(filePath: String, okHttpClient: OkHttpClient)
    fun initializeAsync(filePath: String, okHttpClient: OkHttpClient)
    fun resume()
    fun pause()
    fun release()
}
