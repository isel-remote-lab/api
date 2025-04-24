package isel.rl.core.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    val type: String = "",
    val title: String = "",
    val details: String = "",
) {
    companion object {
        private const val MEDIA_TYPE = "application/problem+json"

        fun response(
            status: Int,
            problem: Problem,
        ): ResponseEntity<Any> = ResponseEntity.status(status).header("Content-Type", MEDIA_TYPE).body(problem)

        // General
        val unexpectedBehaviour =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Unexpected behaviour",
                "An unexpected behaviour occurred",
            )

        // User Related
        val invalidRole =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid role",
                "The role provided is invalid. The role doesn't exist or is not supported. " +
                    "The roles supported are: 'S' for student, 'T' for teacher and 'A' for admin.",
            )

        val invalidUsername =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid username",
                "The username provided is invalid.",
            )

        val invalidEmail =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid email",
                "The email provided is invalid.",
            )

        val invalidOauthId =
            Problem(
                URI(
                    "TODO",
                ).toASCIIString(),
                "Invalid oauthId",
                "The oauthId provided is invalid.",
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

        // Laboratory Related
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
    }
}
