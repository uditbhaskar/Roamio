package com.roamio.application

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.roamio.BuildConfig
import com.roamio.core.constants.CoreConstants
import com.roamio.core.di.coreModules
import com.roamio.di.appModules
import com.roamio.feature.home.di.homeModule
import com.roamio.feature.onboarding.di.onboardingModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * Application entry point that initializes logging and KOIN dependency injection.
 *
 * @author udit
 */
class RoamioApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                okhttp3.OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request()
                        val host = request.url.host
                        val builder = request.newBuilder()
                            .header(
                                CoreConstants.Network.HEADER_USER_AGENT,
                                CoreConstants.Network.USER_AGENT,
                            )
                        if (
                            host.contains(CoreConstants.Api.WIKIMEDIA_HOST) ||
                            host.contains(CoreConstants.Api.WIKIPEDIA_HOST)
                        ) {
                            builder.header(
                                CoreConstants.Api.HEADER_REFERER,
                                CoreConstants.Api.WIKIPEDIA_REFERER,
                            )
                        }
                        chain.proceed(builder.build())
                    }
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .crossfade(CoreConstants.Api.IMAGE_CROSSFADE_MILLIS)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(CoreConstants.Api.IMAGE_MEMORY_CACHE_PERCENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve(CoreConstants.Api.IMAGE_DISK_CACHE_DIR))
                    .maxSizeBytes(CoreConstants.Api.IMAGE_DISK_CACHE_BYTES)
                    .build()
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidLogger()
            androidContext(this@RoamioApp)
            properties(mapOf("PEXELS_API_KEY" to BuildConfig.PEXELS_API_KEY))
            modules(appModules)
            modules(coreModules)
            modules(onboardingModules)
            modules(homeModule)
        }
    }
}
