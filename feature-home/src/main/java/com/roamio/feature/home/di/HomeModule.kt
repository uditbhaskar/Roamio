package com.roamio.feature.home.di

import com.roamio.feature.home.currency.data.CurrencyRepository
import com.roamio.feature.home.currency.viewModel.CurrencyViewModel
import com.roamio.feature.home.data.HomeRepository
import com.roamio.feature.home.data.HomeWarmup
import com.roamio.feature.home.place.data.PlaceRepository
import com.roamio.feature.home.place.viewModel.PlaceViewModel
import com.roamio.feature.home.popular.data.PopularRepository
import com.roamio.feature.home.popular.viewModel.PopularViewModel
import com.roamio.feature.home.saved.viewModel.SavedViewModel
import com.roamio.feature.home.settings.viewModel.SettingsViewModel
import com.roamio.feature.home.viewModel.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin bindings for the post-onboarding explore app.
 *
 * @author udit
 */
val homeModule = module {
    single { HomeRepository(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { HomeWarmup(get()) }
    single { PopularRepository(get(), get(), get()) }
    single { PlaceRepository(get(), get(), get(), get(), get(), get(), get()) }
    single { CurrencyRepository(get()) }
    viewModelOf(::HomeViewModel)
    viewModelOf(::PopularViewModel)
    viewModelOf(::PlaceViewModel)
    viewModelOf(::SavedViewModel)
    viewModelOf(::CurrencyViewModel)
    viewModelOf(::SettingsViewModel)
}
