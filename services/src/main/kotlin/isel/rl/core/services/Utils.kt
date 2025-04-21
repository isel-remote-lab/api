package isel.rl.core.services

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.utils.failure

fun handleException(e: Exception) =
    failure(if (e is ServicesExceptions) e else ServicesExceptions.UnexpectedError)