package com.roamio.core.network

import com.roamio.core.constants.CoreConstants
import com.roamio.core.result.userFacingMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters

/**
 * Posts Overpass QL queries and fails over across public endpoints on 503 and similar errors.
 *
 * @param httpClient Shared Ktor HTTP client from core.
 * @author udit
 */
class OverpassGateway(
    private val httpClient: HttpClient,
) {

    /**
     * Executes [query] against Overpass, trying fallback hosts when the primary is busy.
     *
     * @param query Overpass QL request body.
     * @return Successful HTTP response with JSON elements.
     * @author udit
     */
    suspend fun fetch(query: String): HttpResponse {
        var lastError: Exception? = null
        for (baseUrl in endpoints) {
            try {
                return httpClient.submitForm(
                    url = baseUrl + CoreConstants.Api.OVERPASS_INTERPRETER_PATH,
                    formParameters = Parameters.build {
                        append(CoreConstants.Api.PARAM_DATA, query)
                    },
                ) {
                    timeout {
                        requestTimeoutMillis = CoreConstants.Api.OVERPASS_REQUEST_TIMEOUT_MILLIS
                    }
                }
            } catch (error: Exception) {
                lastError = error
            }
        }
        throw Exception(userFacingMessage(lastError ?: Exception(CoreConstants.Errors.NETWORK)))
    }

    private companion object {
        val endpoints = listOf(
            CoreConstants.Api.OVERPASS_FALLBACK_BASE_URL_KUMI,
            CoreConstants.Api.OVERPASS_BASE_URL,
            CoreConstants.Api.OVERPASS_FALLBACK_BASE_URL,
        )
    }
}
