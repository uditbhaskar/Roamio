package com.roamio.application

import android.app.Application
import com.roamio.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application class for initializing Koin dependency injection on app startup.
 * @author udit
 */
class RoamioApp : Application(){
    override fun onCreate() {
        super.onCreate()
        //start  koin dependency
        startKoin {
            androidLogger()
            androidContext(this@RoamioApp)
            modules(appModule)
        }
    }
}