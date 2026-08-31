package com.roamio.core.di

import com.roamio.core.currency.CurrencyClient
import com.roamio.core.location.LocationProvider
import com.roamio.core.location.PlaceNameResolver
import com.roamio.core.network.HttpClientProvider
import com.roamio.core.network.OverpassGateway
import com.roamio.core.photo.PlacePhotoResolver
import com.roamio.core.places.ExploreSession
import com.roamio.core.places.PlacesClient
import com.roamio.core.places.SelectedPlaceStore
import com.roamio.core.preferences.AppPreferences
import com.roamio.core.weather.WeatherClient
import com.roamio.core.wiki.WikipediaClient
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
    single { AppPreferences(androidContext()) }
    single { LocationProvider(androidContext(), get()) }
    single { WeatherClient(get()) }
    single { WikipediaClient(get()) }
    single { PlacesClient(get()) }
    single { CurrencyClient(get()) }
    single { ExploreSession() }
    single { SelectedPlaceStore() }
    single {
        val key = runCatching { getProperty<String>("PEXELS_API_KEY") }.getOrDefault("")
        PlacePhotoResolver(get(), get(), key)
    }
}
