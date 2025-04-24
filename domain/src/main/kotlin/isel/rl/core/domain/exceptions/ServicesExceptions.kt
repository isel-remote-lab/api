package isel.rl.core.domain.exceptions

/*
 * This file contains the definition of the ServicesExceptions class and its subclasses.
 * The ServicesExceptions class is a sealed class that represents various exceptions that can occur in the services layer.
 * Each subclass represents a specific type of exception.
 * The data object subclasses implement the readResolve method to ensure that they are singleton instances, since Java Serialization calls this method.
 * The UnexpectedError subclass represents an unexpected error that may occur.
 */
sealed class ServicesExceptions(message: String) : Exception(message) {
    data object Users {
        data object InvalidRole : ServicesExceptions("Invalid role") {
            private fun readResolve(): Any = InvalidRole
        }

        data object InvalidUsername : ServicesExceptions("Invalid username") {
            private fun readResolve(): Any = InvalidUsername
        }

        data object InvalidEmail : ServicesExceptions("Invalid email") {
            private fun readResolve(): Any = InvalidEmail
        }

        data object InvalidOauthId : ServicesExceptions("Invalid oauthId") {
            private fun readResolve(): Any = InvalidOauthId
        }

        data object InvalidUserId : ServicesExceptions("Invalid userId") {
            private fun readResolve(): Any = InvalidUserId
        }

        data object UserNotFound : ServicesExceptions("User not found") {
            private fun readResolve(): Any = UserNotFound
        }

        data object InvalidQueryParams : ServicesExceptions("Invalid query params") {
            private fun readResolve(): Any = InvalidQueryParams
        }
    }

    data object Laboratories {
        data object InvalidLaboratoryId : ServicesExceptions("Invalid laboratory id") {
            private fun readResolve(): Any = InvalidLaboratoryId
        }

        data object LaboratoryNotFound : ServicesExceptions("Laboratory not found") {
            private fun readResolve(): Any = LaboratoryNotFound
        }

        data object LaboratoryNotOwned : ServicesExceptions("Laboratory not owned") {
            private fun readResolve(): Any = LaboratoryNotOwned
        }

        class InvalidLaboratoryName(message: String) : ServicesExceptions(message)

        class InvalidLaboratoryDescription(message: String) : ServicesExceptions(message)

        class InvalidLaboratoryDuration(message: String) : ServicesExceptions(message)

        class InvalidLaboratoryQueueLimit(message: String) : ServicesExceptions(message)
    }

    data object UnexpectedError : ServicesExceptions("Unexpected error") {
        private fun readResolve(): Any = UnexpectedError
    }
}
