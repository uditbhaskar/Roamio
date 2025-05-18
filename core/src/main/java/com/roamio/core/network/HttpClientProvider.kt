package com.roamio.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import timber.log.Timber

private const val TIME_OUT = 60000L
private const val KTOR_LOGGER = "Ktor_Logger:"
private const val HTTP_STATUS = "Http status::"
private const val HTTP_EXCEPTION = "HTTP error: "

/**
 * Provides a pre-configured Ktor HttpClient for Android with JSON serialization,
 * logging, timeout, default headers, and response validation.
 *
 * @author udit
 */
@OptIn(ExperimentalSerializationApi::class)
object HttpClientProvider {

    /**
     * Creates and configures a Ktor HttpClient for Android with:
     * - JSON serialization (pretty print, lenient, ignores unknown keys)
     * - Request timeout
     * - Full HTTP logging via Timber
     * - Response status logging
     * - Default Content-Type header as application/json
     * - Throws exception for non-successful HTTP responses (status >= 300)
     *
     * @return Configured HttpClient instance
     * @author udit
     */
    fun create(): HttpClient = HttpClient(Android) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }


        install(HttpTimeout) {
            requestTimeoutMillis = TIME_OUT
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Timber.tag(KTOR_LOGGER).v(message)
                }
            }
            level = LogLevel.ALL
        }

        install(ResponseObserver) {
            onResponse { response ->
                Timber.tag(HTTP_STATUS).d("${response.status.value}")
            }
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        HttpResponseValidator {
            validateResponse { response ->
                val statusCode = response.status.value
                if (statusCode < 300) return@validateResponse
                else throw Exception("$HTTP_EXCEPTION$statusCode")
            }
        }
    }
}