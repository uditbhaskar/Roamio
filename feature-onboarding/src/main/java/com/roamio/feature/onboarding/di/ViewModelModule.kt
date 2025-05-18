package com.roamio.feature.onboarding.di

import com.roamio.feature.onboarding.viewModel.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module providing the OnboardingViewModel instance for DI.
 * @author udit
 */
val viewModelModule = module {
    viewModelOf(::OnboardingViewModel)
}