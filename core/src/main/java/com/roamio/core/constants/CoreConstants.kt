package com.roamio.core.constants

/**
 * All constants for the core module organized by type.
 *
 * @author udit
 */
object CoreConstants {
    
    object Network {
        const val REQUEST_TIMEOUT_MILLIS = 60000L
        const val HTTP_STATUS_CODE_THRESHOLD = 300
        const val TIMESTAMP_FORMAT = "HH:mm:ss.SSS"
        const val HTTP_ERROR_MESSAGE = "HTTP error: %d"
    }
    
    object Logging {
        const val API_LOGGER_TAG = "🌐 API_CALLS"
        const val REQUEST_PREFIX = "🚀 REQUEST"
        const val RESPONSE_PREFIX = "📥 RESPONSE"
        const val TIMING_PREFIX = "⏱️ TIMING"
        const val LOG_SEPARATOR = "═══════════════════════════════════════════════════════════════"
        const val SINGLE_SPACE = " "
        const val DOUBLE_SPACE = "  "
        const val STATUS_FORMAT = "Status: %s %d - %s"
        const val REQUEST_ID_FORMAT = "req_%d_%d"
        const val REQUEST_KEYWORD = "REQUEST:"
        const val RESPONSE_KEYWORD = "RESPONSE:"
        const val HEADERS_KEYWORD = "HEADERS:"
        const val BODY_KEYWORD = "BODY:"
        const val HEADERS_ICON = "📋"
        const val BODY_ICON = "📄"
        const val INFO_ICON = "ℹ️"
        const val STATUS_OK_EMOJI = "✅"
        const val STATUS_REDIRECT_EMOJI = "🔄"
        const val STATUS_CLIENT_ERROR_EMOJI = "⚠️"
        const val STATUS_SERVER_ERROR_EMOJI = "💥"
        const val STATUS_UNKNOWN_EMOJI = "❓"
        const val STATUS_RANGE_OK_START = 200
        const val STATUS_RANGE_OK_END = 299
        const val STATUS_RANGE_REDIRECT_START = 300
        const val STATUS_RANGE_REDIRECT_END = 399
        const val STATUS_RANGE_CLIENT_ERROR_START = 400
        const val STATUS_RANGE_CLIENT_ERROR_END = 499
        const val STATUS_RANGE_SERVER_ERROR_START = 500
        const val STATUS_RANGE_SERVER_ERROR_END = 599
        const val RANDOM_ID_BOUND = 1000
    }
}
