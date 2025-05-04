package isel.rl.core.services.utils

import isel.rl.core.domain.LimitAndSkip
import isel.rl.core.domain.exceptions.ServicesExceptions

/**
 * Verifies the query parameters.
 *
 * @param limit the limit query parameter
 * @param skip the skip query parameter
 * @return the result of verifying the query
 */
fun verifyQuery(
    limit: String?,
    skip: String?,
): LimitAndSkip {
    var convertedLimit: Int? = null
    if (limit != null) {
        convertedLimit = limit.toIntOrNull()
        if (convertedLimit == null || convertedLimit < 0) {
            throw ServicesExceptions.InvalidQueryParam(
                "Invalid limit query parameter: $limit. Must be a positive integer.",
            )
        }
    }

    var convertedSkip: Int? = null
    if (skip != null) {
        convertedSkip = skip.toIntOrNull()
        if (convertedSkip == null || convertedSkip < 0) {
            throw ServicesExceptions.InvalidQueryParam(
                "Invalid skip query parameter: $skip. Must be a positive integer.",
            )
        }
    }

    return LimitAndSkip(
        limit = convertedLimit ?: LimitAndSkip.DEFAULT_LIMIT,
        skip = convertedSkip ?: LimitAndSkip.DEFAULT_SKIP,
    )
}