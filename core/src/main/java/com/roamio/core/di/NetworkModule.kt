package com.roamio.core.di

import com.roamio.core.location.LocationProvider
import com.roamio.core.location.PlaceNameResolver
import com.roamio.core.network.HttpClientProvider
import com.roamio.core.network.OverpassGateway
import com.roamio.core.preferences.OnboardingPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module providing core network, preferences, and location dependencies.
 *
 * @author udit
 */
val coreNetworkModule = module {
    single { HttpClientProvider.create() }
    single { OverpassGateway(get()) }
    single { PlaceNameResolver(androidContext(), get()) }
    single { OnboardingPreferences(androidContext()) }
    single { LocationProvider(androidContext(), get()) }
}
