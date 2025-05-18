package com.roamio.core.di

import com.roamio.core.network.HttpClientProvider
import org.koin.dsl.module

/**
 * Koin module providing a singleton instance of the shared Ktor HttpClient.
 * @author udit
 */
val coreNetworkModule = module {
    single { HttpClientProvider.create() }
}