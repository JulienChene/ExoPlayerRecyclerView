package io.snaker.app.exoplayerprototype

import android.content.Context
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.util.Util
import io.snaker.app.video.helper.SimpleCacheProvider
import okhttp3.OkHttpClient
import java.io.File

/**
 * Data source to provide Caching capabilities to ExoPlayerExtensions.
 */
class CacheDataSourceFactory(
        private val context: Context,
        okHttpClient: OkHttpClient,
        private val maxCacheSize: Long,
        private val maxFileSize: Long) : DataSource.Factory {

    private val defaultDataSourceFactory: DefaultDataSourceFactory

    init {
        val userAgent = Util.getUserAgent(context, "VideoRecyclerView")
        val bandwidthMeter = DefaultBandwidthMeter()
        defaultDataSourceFactory = DefaultDataSourceFactory(this.context, bandwidthMeter,
                OkHttpDataSourceFactory(okHttpClient, userAgent, bandwidthMeter))
    }

    override fun createDataSource(): DataSource {
        val evictor = LeastRecentlyUsedCacheEvictor(maxCacheSize)
        val simpleCache = SimpleCacheProvider.getInstance(File(context.cacheDir, "media"), evictor)
        return CacheDataSource(simpleCache, defaultDataSourceFactory.createDataSource(),
                FileDataSource(), CacheDataSink(simpleCache, maxFileSize),
                CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null)
    }
}
