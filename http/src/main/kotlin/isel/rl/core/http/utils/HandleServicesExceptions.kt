package isel.rl.core.http.utils

import isel.rl.core.domain.exceptions.ServicesExceptions
import isel.rl.core.http.model.Problem
import org.springframework.http.ResponseEntity

fun handleServicesExceptions(exception: ServicesExceptions): ResponseEntity<*> =
    when (exception) {
        /**
         * Users Exceptions
         */
        ServicesExceptions.Users.InvalidEmail -> Problem.response(400, Problem.invalidEmail)
        ServicesExceptions.Users.InvalidRole -> Problem.response(400, Problem.invalidRole)
        ServicesExceptions.Users.InvalidName -> Problem.response(400, Problem.invalidName)
        ServicesExceptions.Users.InvalidUserId -> Problem.response(400, Problem.invalidUserId)
        ServicesExceptions.Users.UserNotFound -> Problem.response(404, Problem.userNotFound)
        ServicesExceptions.Users.ErrorWhenUpdatingUser ->
            Problem.response(
                400,
                Problem.errorWhenUpdatingUser,
            )

        /**
         * Laboratories Exceptions
         */
        is ServicesExceptions.Laboratories.InvalidLaboratoryName ->
            Problem.response(
                400,
                Problem.invalidLaboratoryName(
                    exception.message!!,
                ),
            )

        is ServicesExceptions.Laboratories.InvalidLaboratoryDescription ->
            Problem.response(
                400,
                Problem.invalidLaboratoryDescription(
                    exception.message!!,
                ),
            )

        is ServicesExceptions.Laboratories.InvalidLaboratoryDuration ->
            Problem.response(
                400,
                Problem.invalidLaboratoryDuration(
                    exception.message!!,
                ),
            )

        is ServicesExceptions.Laboratories.InvalidLaboratoryQueueLimit ->
            Problem.response(
                400,
                Problem.invalidLaboratoryQueueLimit(
                    exception.message!!,
                ),
            )

        is ServicesExceptions.Laboratories.InvalidLaboratoryId -> Problem.response(400, Problem.invalidLaboratoryId)
        ServicesExceptions.Laboratories.LaboratoryNotFound -> Problem.response(404, Problem.laboratoryNotFound)

        /**
         * Groups Exceptions
         */
        ServicesExceptions.Groups.InvalidGroupId -> Problem.response(400, Problem.invalidGroupId)
        ServicesExceptions.Groups.GroupNotFound -> Problem.response(404, Problem.groupNotFound)
        ServicesExceptions.Groups.UserAlreadyInGroup -> Problem.response(400, Problem.userAlreadyInGroup)
        ServicesExceptions.Groups.UserNotInGroup -> Problem.response(400, Problem.userNotInGroup)
        is ServicesExceptions.Groups.InvalidGroupName -> Problem.response(400, Problem.invalidGroupName(exception.message!!))
        is ServicesExceptions.Groups.InvalidGroupDescription ->
            Problem.response(
                400,
                Problem.invalidGroupDescription(exception.message!!),
            )

        /**
         * Hardware Exceptions
         */

        ServicesExceptions.Hardware.HardwareNotFound -> Problem.response(404, Problem.hardwareNotFound)
        ServicesExceptions.Hardware.InvalidHardwareId -> Problem.response(400, Problem.invalidHardwareId)
        is ServicesExceptions.Hardware.InvalidHardwareIpAddress ->
            Problem.response(
                400,
                Problem.invalidHardwareIpAddress(exception.message!!),
            )
        is ServicesExceptions.Hardware.InvalidHardwareMacAddress ->
            Problem.response(
                400,
                Problem.invalidHardwareMacAddress(exception.message!!),
            )
        is ServicesExceptions.Hardware.InvalidHardwareName -> Problem.response(400, Problem.invalidHardwareName(exception.message!!))
        is ServicesExceptions.Hardware.InvalidHardwareSerialNumber ->
            Problem.response(
                400,
                Problem.invalidHardwareSerialNumber(exception.message!!),
            )
        is ServicesExceptions.Hardware.InvalidHardwareStatus -> Problem.response(400, Problem.invalidHardwareStatus(exception.message!!))

        is ServicesExceptions.Forbidden -> Problem.response(403, Problem.forbidden(exception.message!!))
        is ServicesExceptions.InvalidQueryParam -> Problem.response(400, Problem.invalidQueryParam(exception.message!!))
        ServicesExceptions.UnexpectedError -> Problem.response(500, Problem.unexpectedError)
    }
