package isel.rl.core.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    val type: String = "",
    val title: String = "",
    val detail: String = "",
) {
    companion object {
        private const val MEDIA_TYPE = "application/problem+json"

        fun response(
            status: Int,
            problem: Problem,
        ): ResponseEntity<Any> = ResponseEntity.status(status).header("Content-Type", MEDIA_TYPE).body(problem)

        /**
         * Common Problems
         */

        fun forbidden(message: String) =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Forbidden",
                message,
            )

        val unexpectedError =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Unexpected error",
                "An unexpected error occurred",
            )

        fun invalidQueryParam(message: String) =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid query parameters",
                message,
            )

        /**
         * Users Related
         */

        val invalidRole =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid role",
                "The role provided is invalid. The role doesn't exist or is not supported. " +
                    "The roles supported are: 'S' for student, 'T' for teacher and 'A' for admin.",
            )

        val invalidName =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid name",
                "The name provided is invalid.",
            )

        val invalidEmail =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid email",
                "The email provided is invalid.",
            )

        val invalidUserId =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid userId",
                "The userId provided is invalid. It should be a number.",
            )

        val userNotFound =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "User not found",
                "The user with the provided information was not found.",
            )

        val errorWhenUpdatingUser =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Error when updating user",
                "An error occurred when updating the user.",
            )

        /**
         * Laboratories Related
         */
        val invalidLaboratoryId =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid laboratory id",
                "The laboratory id provided is invalid. It should be a number.",
            )

        val laboratoryNotFound =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Laboratory not found",
                "The laboratory with the provided information was not found.",
            )

        val laboratoryNotOwned =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Laboratory not owned",
                "The laboratory with the provided information is not owned by the user.",
            )

        fun invalidLaboratoryName(message: String) =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid laboratory name",
                message,
            )

        fun invalidLaboratoryDescription(message: String) =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid laboratory description",
                message,
            )

        fun invalidLaboratoryDuration(message: String) =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid laboratory duration",
                message,
            )

        fun invalidLaboratoryQueueLimit(message: String) =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid laboratory queue limit",
                message,
            )

        /**
         * Groups Related
         */
        val invalidGroupId =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid group id",
                "The group id provided is invalid. It should be a number.",
            )

        val groupNotFound =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Group not found",
                "The group with the provided information was not found.",
            )

        val userAlreadyInGroup =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "User already in group",
                "The user with the provided information is already in the group.",
            )

        val userNotInGroup =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "User not in group",
                "The user with the provided information is not in the group.",
            )

        fun invalidGroupName(message: String) =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid group name",
                message,
            )

        fun invalidGroupDescription(message: String) =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid group description",
                message,
            )
    }
}
