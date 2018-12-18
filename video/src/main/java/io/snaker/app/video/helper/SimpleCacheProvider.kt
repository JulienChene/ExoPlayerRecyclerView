package io.snaker.app.exoplayerprototype

import com.google.android.exoplayer2.upstream.cache.CacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache

import java.io.File

/**
 * SimpleCacheProvider is here to provide a single instance of SimpleCache to
 * CacheDataSourceFactory. Otherwise, a new instance would be created for every new instance of
 * CacheDataSourceFactory and we could end up with SimpleCache returning the same data for
 * different inputs
 */
class SimpleCacheProvider private constructor() {
    companion object : SingletonHolder<SimpleCache, File, CacheEvictor>(::SimpleCache)
}
