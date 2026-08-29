package com.roamio.core.result

import com.roamio.core.constants.CoreConstants

/**
 * Shared result wrapper for repository and use-case operations.
 *
 * @author udit
 */
sealed class AppResult<out T> {

    /**
     * Successful outcome carrying typed payload data.
     *
     * @author udit
     */
    data class Success<T>(val data: T) : AppResult<T>()

    /**
     * Failed outcome with a user-facing message and optional cause.
     *
     * @author udit
     */
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>()
}

/**
 * Executes [block] and maps success or failure into [AppResult].
 *
 * @param block Suspend lambda to execute safely.
 * @return [AppResult.Success] with the block result, or [AppResult.Error] on exception.
 * @author udit
 */
suspend inline fun <T> runAppCatching(block: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (e: Exception) {
        AppResult.Error(
            message = userFacingMessage(e),
            cause = e,
        )
    }
}

/**
 * Maps a thrown error to a short message that is safe to show in the UI.
 *
 * @param error The caught failure.
 * @return A user-facing error string from [CoreConstants.Errors].
 * @author udit
 */
fun userFacingMessage(error: Throwable): String {
    val raw = error.message.orEmpty()
    return when {
        raw == CoreConstants.Errors.NETWORK -> raw
        raw == CoreConstants.Errors.SERVICE_BUSY -> raw
        raw == CoreConstants.Errors.EMPTY_RESULTS -> raw
        raw == CoreConstants.Errors.UNKNOWN -> raw
        raw.contains(CoreConstants.Errors.HTTP_UNAVAILABLE) -> CoreConstants.Errors.SERVICE_BUSY
        raw.contains(CoreConstants.Errors.HTTP_TOO_MANY_REQUESTS) -> CoreConstants.Errors.SERVICE_BUSY
        raw.contains(CoreConstants.Errors.HTTP_GATEWAY_TIMEOUT) -> CoreConstants.Errors.SERVICE_BUSY
        else -> CoreConstants.Errors.NETWORK
    }
}
