package isel.rl.core.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

data class Problem(
    val type: String = "",
    val title: String = "",
    val detail: String = "",
) {
    companion object {
        private const val MEDIA_TYPE = "application/problem+json"
        private const val FILE_TYPE_SUFFIX = ".md"
        private const val PREFIX_TYPE_URI = "https://github.com/isel-remote-lab/documentation/blob/main/problems/"

        fun response(
            status: Int,
            problem: Problem,
        ): ResponseEntity<Any> = ResponseEntity.status(status).header("Content-Type", MEDIA_TYPE).body(problem)

        fun stringResponse(
            type: String,
            title: String,
            detail: String,
        ) = """
            {
                "type": "${PREFIX_TYPE_URI + type + FILE_TYPE_SUFFIX}",
                "title": "$title",
                "detail": "$detail"
            }
            """.trimIndent()

        private fun type(type: String): String = URI(PREFIX_TYPE_URI + type + FILE_TYPE_SUFFIX).toASCIIString()

        /**
         * Common Problems
         */
        fun forbidden(message: String) =
            Problem(
                type("forbidden"),
                "Forbidden",
                message,
            )

        fun unauthorized(message: String) =
            Problem(
                type("unauthorized"),
                "Unauthorized",
                message,
            )

        val unexpectedError =
            Problem(
                type("unexpected-error"),
                "Unexpected error",
                "An unexpected error occurred",
            )

        fun invalidQueryParam(message: String) =
            Problem(
                type("invalid-query-param"),
                "Invalid query parameters",
                message,
            )

        /**
         * Users Related
         */

        val invalidRole =
            Problem(
                type("invalid-role"),
                "Invalid role",
                "The role provided is invalid",
            )

        val invalidName =
            Problem(
                type("invalid-name"),
                "Invalid name",
                "The name provided is invalid.",
            )

        val invalidEmail =
            Problem(
                type("invalid-email"),
                "Invalid email",
                "The email provided is invalid.",
            )

        val invalidUserId =
            Problem(
                type("invalid-user-id"),
                "Invalid userId",
                "The userId provided is invalid. It should be a number.",
            )

        val userNotFound =
            Problem(
                type("user-not-found"),
                "User not found",
                "The user with the provided information was not found.",
            )

        val errorWhenUpdatingUser =
            Problem(
                type("error-when-updating-user"),
                "Error when updating user",
                "An error occurred when updating the user.",
            )

        /**
         * Laboratories Related
         */
        val invalidLaboratoryId =
            Problem(
                type("invalid-laboratory-id"),
                "Invalid laboratory id",
                "The laboratory id provided is invalid. It should be a number.",
            )

        val laboratoryNotFound =
            Problem(
                type("laboratory-not-found"),
                "Laboratory not found",
                "The laboratory with the provided information was not found.",
            )

        fun invalidLaboratoryName(message: String) =
            Problem(
                type("invalid-laboratory-name"),
                "Invalid laboratory name",
                message,
            )

        fun invalidLaboratoryDescription(message: String) =
            Problem(
                type("invalid-laboratory-description"),
                "Invalid laboratory description",
                message,
            )

        fun invalidLaboratoryDuration(message: String) =
            Problem(
                type("invalid-laboratory-duration"),
                "Invalid laboratory duration",
                message,
            )

        fun invalidLaboratoryQueueLimit(message: String) =
            Problem(
                type("invalid-laboratory-queue-limit"),
                "Invalid laboratory queue limit",
                message,
            )

        val groupNotFoundInLaboratory =
            Problem(
                type("group-not-found-in-laboratory"),
                "Group not found in laboratory",
                "The group with the provided information was not found in the laboratory.",
            )

        val hardwareNotFoundInLaboratory =
            Problem(
                type("hardware-not-found-in-laboratory"),
                "Hardware not found in laboratory",
                "The hardware with the provided information was not found in the laboratory.",
            )

        val groupAlreadyInLaboratory =
            Problem(
                type("group-already-in-laboratory"),
                "Group already in laboratory",
                "The group with the provided information is already in the laboratory.",
            )

        val hardwareAlreadyInLaboratory =
            Problem(
                type("hardware-already-in-laboratory"),
                "Hardware already in laboratory",
                "The hardware with the provided information is already in the laboratory.",
            )

        /**
         * Groups Related
         */
        val invalidGroupId =
            Problem(
                type("invalid-group-id"),
                "Invalid group id",
                "The group id provided is invalid. It should be a number.",
            )

        val groupNotFound =
            Problem(
                type("group-not-found"),
                "Group not found",
                "The group with the provided information was not found.",
            )

        val userAlreadyInGroup =
            Problem(
                type("user-already-in-group"),
                "User already in group",
                "The user with the provided information is already in the group.",
            )

        val userNotInGroup =
            Problem(
                type("user-not-in-group"),
                "User not in group",
                "The user with the provided information is not in the group.",
            )

        val cantRemoveOwner =
            Problem(
                type("cant-remove-owner"),
                "cant-remove-owner",
                "Cant remove owner from group",
            )

        fun invalidGroupName(message: String) =
            Problem(
                type("invalid-group-name"),
                "Invalid group name",
                message,
            )

        fun invalidGroupDescription(message: String) =
            Problem(
                type("invalid-group-description"),
                "Invalid group description",
                message,
            )

        /**
         * Hardware Related
         */
        val hardwareNotFound =
            Problem(
                type("hardware-not-found"),
                "Hardware not found",
                "The hardware with the provided information was not found.",
            )

        val invalidHardwareId =
            Problem(
                type("invalid-hardware-id"),
                "Invalid hardware id",
                "The hardware with the provided information was not found.",
            )

        fun invalidHardwareIpAddress(message: String) =
            Problem(
                type("invalid-hardware-ip-address"),
                "Invalid hardware ip address",
                message,
            )

        fun invalidHardwareMacAddress(message: String) =
            Problem(
                type("invalid-hardware-mac-address"),
                "Invalid hardware mac address",
                message,
            )

        fun invalidHardwareName(message: String) =
            Problem(
                type("invalid-hardware-name"),
                "Invalid hardware name",
                message,
            )

        fun invalidHardwareSerialNumber(message: String) =
            Problem(
                type("invalid-hardware-serial-number"),
                "Invalid hardware number",
                message,
            )

        fun invalidHardwareStatus(message: String) =
            Problem(
                type("invalid-hardware-status"),
                "Invalid hardware status",
                message,
            )
    }
}
