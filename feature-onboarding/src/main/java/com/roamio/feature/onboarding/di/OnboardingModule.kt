package com.roamio.feature.onboarding.di

import org.koin.dsl.module

/**
 * Aggregates all onboarding Koin modules for dependency injection.
 * @author udit
 */
val onboardingModules = module {
    vieModelModule
}