package com.roamio.core.network

import com.roamio.core.constants.CoreConstants
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * Provides a pre-configured Ktor HttpClient for Android with enhanced JSON serialization,
 * comprehensive logging, timeout, default headers, and response validation.
 *
 * Features:
 * - JSON serialization with pretty print and lenient parsing
 * - Request timeout configuration
 * - Beautiful API logging with request/response details
 * - Default Content-Type header as application/json
 * - Response validation for non-successful HTTP responses
 *
 * @author udit
 */
@OptIn(ExperimentalSerializationApi::class)
object HttpClientProvider {

    /**
     * Creates and configures a Ktor HttpClient for Android with comprehensive features.
     *
     * @return Configured HttpClient instance ready for API calls
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
            requestTimeoutMillis = CoreConstants.Network.REQUEST_TIMEOUT_MILLIS
        }

        install(Logging, BeautifulLoggingConfig.createLoggingConfig())
        
        install(ResponseObserver, BeautifulLoggingConfig.createResponseObserverConfig())

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        HttpResponseValidator {
            validateResponse { response ->
                val statusCode = response.status.value
                if (statusCode < CoreConstants.Network.HTTP_STATUS_CODE_THRESHOLD) return@validateResponse
                else {
                    throw Exception(String.format(CoreConstants.Network.HTTP_ERROR_MESSAGE, statusCode))
                }
            }
        }
    }
}