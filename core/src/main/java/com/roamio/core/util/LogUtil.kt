package com.roamio.core.util

import com.roamio.core.constants.CoreConstants
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for beautiful, consistent logging across the app.
 * Provides structured logging with emojis, timestamps, and request tracking.
 *
 * @author udit
 */
object LogUtil {
    
    private val timestampFormat = SimpleDateFormat(CoreConstants.Network.TIMESTAMP_FORMAT, Locale.US)
    
    /**
     * Logs API request information with beautiful formatting.
     *
     * @param message The request message
     * @param requestId Optional request ID for tracking
     * @author udit
     */
    fun logRequest(message: String, requestId: String? = null) {
        val timestamp = timestampFormat.format(Date())
        val id = requestId ?: generateRequestId()
        
        Timber.tag(CoreConstants.Logging.API_LOGGER_TAG).d(
            buildLogMessage(
                header = "${CoreConstants.Logging.REQUEST_PREFIX}${CoreConstants.Logging.SINGLE_SPACE}[$timestamp] - ID: $id",
                content = message
            )
        )
    }
    
    /**
     * Logs API response information with beautiful formatting.
     *
     * @param message The response message
     * @param requestId Optional request ID for tracking
     * @param duration Optional request duration in milliseconds
     * @author udit
     */
    fun logResponse(message: String, requestId: String? = null, duration: Long? = null) {
        val timestamp = timestampFormat.format(Date())
        val id = requestId ?: "unknown"
        val durationText = duration?.let { "${CoreConstants.Logging.SINGLE_SPACE}(${it}ms)" } ?: ""
        
        Timber.tag(CoreConstants.Logging.API_LOGGER_TAG).d(
            buildLogMessage(
                header = "${CoreConstants.Logging.RESPONSE_PREFIX}${CoreConstants.Logging.SINGLE_SPACE}[$timestamp] - ID: $id$durationText",
                content = message
            )
        )
    }
    
    /**
     * Logs API timing information.
     *
     * @param statusCode HTTP status code
     * @param statusMessage HTTP status message
     * @param requestId Optional request ID for tracking
     * @author udit
     */
    fun logTiming(statusCode: Int, statusMessage: String, requestId: String? = null) {
        val timestamp = timestampFormat.format(Date())
        val id = requestId ?: "unknown"
        val statusEmoji = getStatusEmoji(statusCode)
        
        val content = String.format(
            Locale.US,
            CoreConstants.Logging.STATUS_FORMAT,
            statusEmoji,
            statusCode,
            statusMessage
        )
        
        Timber.tag(CoreConstants.Logging.API_LOGGER_TAG).d(
            "${CoreConstants.Logging.TIMING_PREFIX}${CoreConstants.Logging.SINGLE_SPACE}[$timestamp] - ID: $id\n$content"
        )
    }
    
    /**
     * Logs headers with icon.
     *
     * @param message The headers message
     * @author udit
     */
    fun logHeaders(message: String) {
        Timber.tag(CoreConstants.Logging.API_LOGGER_TAG).v("${CoreConstants.Logging.HEADERS_ICON}${CoreConstants.Logging.SINGLE_SPACE}$message")
    }
    
    /**
     * Logs body content with icon.
     *
     * @param message The body message
     * @author udit
     */
    fun logBody(message: String) {
        Timber.tag(CoreConstants.Logging.API_LOGGER_TAG).v("${CoreConstants.Logging.BODY_ICON}${CoreConstants.Logging.SINGLE_SPACE}$message")
    }
    
    /**
     * Logs general info with icon.
     *
     * @param message The info message
     * @author udit
     */
    fun logInfo(message: String) {
        Timber.tag(CoreConstants.Logging.API_LOGGER_TAG).v("${CoreConstants.Logging.INFO_ICON}${CoreConstants.Logging.DOUBLE_SPACE}$message")
    }
    
    /**
     * Builds a formatted log message with separator and header.
     *
     * @param separator The separator line
     * @param header The header text
     * @param content The main content
     * @return Formatted log message
     * @author udit
     */
    private fun buildLogMessage(separator: String = CoreConstants.Logging.LOG_SEPARATOR, header: String, content: String): String {
        return "$separator\n$header\n$separator\n$content\n$separator"
    }
    
    /**
     * Returns appropriate emoji for HTTP status codes.
     *
     * @param statusCode The HTTP status code
     * @return Emoji string representing the status
     * @author udit
     */
    private fun getStatusEmoji(statusCode: Int): String {
        return when (statusCode) {
            in CoreConstants.Logging.STATUS_RANGE_OK_START..CoreConstants.Logging.STATUS_RANGE_OK_END -> CoreConstants.Logging.STATUS_OK_EMOJI
            in CoreConstants.Logging.STATUS_RANGE_REDIRECT_START..CoreConstants.Logging.STATUS_RANGE_REDIRECT_END -> CoreConstants.Logging.STATUS_REDIRECT_EMOJI
            in CoreConstants.Logging.STATUS_RANGE_CLIENT_ERROR_START..CoreConstants.Logging.STATUS_RANGE_CLIENT_ERROR_END -> CoreConstants.Logging.STATUS_CLIENT_ERROR_EMOJI
            in CoreConstants.Logging.STATUS_RANGE_SERVER_ERROR_START..CoreConstants.Logging.STATUS_RANGE_SERVER_ERROR_END -> CoreConstants.Logging.STATUS_SERVER_ERROR_EMOJI
            else -> CoreConstants.Logging.STATUS_UNKNOWN_EMOJI
        }
    }
    
    /**
     * Generates a unique request identifier for tracking API calls.
     *
     * @return Unique request ID string
     * @author udit
     */
    private fun generateRequestId(): String {
        return String.format(
            Locale.US,
            CoreConstants.Logging.REQUEST_ID_FORMAT,
            System.currentTimeMillis(),
            kotlin.random.Random.nextInt(CoreConstants.Logging.RANDOM_ID_BOUND)
        )
    }
}