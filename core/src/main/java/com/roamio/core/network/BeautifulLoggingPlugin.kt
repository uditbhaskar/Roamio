package com.roamio.core.network

import com.roamio.core.constants.CoreConstants
import com.roamio.core.util.LogUtil
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver

/**
 * Beautiful logging configuration for Ktor HttpClient.
 * Provides enhanced logging with request/response details, timing, and pretty-printed JSON.
 *
 * This configuration enhances the standard Ktor logging with:
 * - Beautiful formatting with emojis and clear sections
 * - Request and response timing
 * - Pretty-printed JSON bodies
 * - Unique request IDs for tracking
 * - Comprehensive error logging
 *
 * @author udit
 */
object BeautifulLoggingConfig {
    
    /**
     * Creates a beautiful logging configuration for Ktor HttpClient.
     *
     * @return Logging configuration with enhanced formatting
     * @author udit
     */
    fun createLoggingConfig(): Logging.Config.() -> Unit = {
        logger = object : Logger {
            override fun log(message: String) {
                when {
                    message.contains(CoreConstants.Logging.REQUEST_KEYWORD) -> {
                        LogUtil.logRequest(message)
                    }
                    message.contains(CoreConstants.Logging.RESPONSE_KEYWORD) -> {
                        LogUtil.logResponse(message)
                    }
                    message.contains(CoreConstants.Logging.HEADERS_KEYWORD) -> {
                        LogUtil.logHeaders(message)
                    }
                    message.contains(CoreConstants.Logging.BODY_KEYWORD) -> {
                        LogUtil.logBody(message)
                    }
                    else -> {
                        LogUtil.logInfo(message)
                    }
                }
            }
        }
        level = LogLevel.ALL
    }
    
    /**
     * Creates a response observer configuration for enhanced response logging.
     *
     * @return ResponseObserver configuration
     * @author udit
     */
    fun createResponseObserverConfig(): ResponseObserver.Config.() -> Unit = {
        onResponse { response ->
            val statusCode = response.status.value
            val statusMessage = response.status.description
            LogUtil.logTiming(statusCode, statusMessage)
        }
    }
}
