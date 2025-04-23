package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.utils.failure

/**
 * Utility function to handle exceptions in a consistent way.
 *
 * @param e The exception to handle.
 * @return A [failure] result containing the exception.
 */
fun handleException(e: Exception) =
    failure(if (e is ServicesExceptions) e else ServicesExceptions.UnexpectedError)