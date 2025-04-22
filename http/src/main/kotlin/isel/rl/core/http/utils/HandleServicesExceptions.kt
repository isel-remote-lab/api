package isel.rl.core.http.utils

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.http.model.Problem
import org.springframework.http.ResponseEntity

fun handleServicesExceptions(
    exception: ServicesExceptions,
): ResponseEntity<*> = when (exception) {
    /**
     * Users Exceptions
     */
    ServicesExceptions.Users.InvalidEmail -> Problem.response(400, Problem.invalidEmail)
    ServicesExceptions.Users.InvalidRole -> Problem.response(400, Problem.invalidRole)
    ServicesExceptions.Users.InvalidUsername -> Problem.response(400, Problem.invalidUsername)
    ServicesExceptions.Users.InvalidOauthId -> Problem.response(400, Problem.invalidOauthId)
    ServicesExceptions.Users.InvalidUserId -> Problem.response(400, Problem.invalidUserId)
    ServicesExceptions.Users.UserNotFound -> Problem.response(404, Problem.userNotFound)

    /**
     * Laboratories Exceptions
     */
    is ServicesExceptions.Laboratories.InvalidLaboratoryName -> Problem.response(
        400, Problem.invalidLaboratoryName(
            exception.message!!
        )
    )

    is ServicesExceptions.Laboratories.InvalidLaboratoryDescription -> Problem.response(
        400, Problem.invalidLaboratoryDescription(
            exception.message!!
        )
    )

    is ServicesExceptions.Laboratories.InvalidLaboratoryDuration -> Problem.response(
        400, Problem.invalidLaboratoryDuration(
            exception.message!!
        )
    )

    is ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit -> Problem.response(
        400, Problem.invalidLaboratoryQueueLimit(
            exception.message!!
        )
    )

    ServicesExceptions.Laboratories.InvalidLaboratoryId -> Problem.response(400, Problem.invalidLaboratoryId)
    ServicesExceptions.Laboratories.LaboratoryNotFound -> Problem.response(404, Problem.laboratoryNotFound)
    ServicesExceptions.Laboratories.LaboratoryNotOwned -> Problem.response(403, Problem.laboratoryNotOwned)

    else -> Problem.response(500, Problem.unexpectedBehaviour)
}
