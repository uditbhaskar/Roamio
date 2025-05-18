package com.roamio.application

import android.app.Application
import com.roamio.BuildConfig
import com.roamio.core.di.coreModules
import com.roamio.di.appModules
import com.roamio.feature.onboarding.di.onboardingModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * Application class for initializing Koin dependency injection on app startup.
 * @author udit
 */
class RoamioApp : Application(){
    override fun onCreate() {
        super.onCreate()
        //Plant timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        //start  koin dependency
        startKoin {
            androidLogger()
            androidContext(this@RoamioApp)
            modules(appModules)
            modules(coreModules)
            modules(onboardingModules)
        }
    }
}