package isel.rl.core.domain.exceptions

/**
 * [ServicesExceptions] is a sealed class that represents various exceptions that can occur in the services layer.
 * Each subclass represents a specific type of exception.
 * The data object subclasses implement the readResolve method to ensure that they are singleton instances, since Java Serialization calls this method
 * and they are data objects because they are used to represent a single instance of the exception.
 * There are other types of services exceptions that are not data objects but class because they carry a message.
 * The UnexpectedError subclass represents an unexpected error that may occur.
 */
sealed class ServicesExceptions(message: String = "") : Exception(message) {
    data object Users {
        data object InvalidRole : ServicesExceptions() {
            private fun readResolve(): Any = InvalidRole
        }

        data object InvalidName : ServicesExceptions() {
            private fun readResolve(): Any = InvalidName
        }

        data object InvalidEmail : ServicesExceptions() {
            private fun readResolve(): Any = InvalidEmail
        }

        data object InvalidUserId : ServicesExceptions() {
            private fun readResolve(): Any = InvalidUserId
        }

        data object UserNotFound : ServicesExceptions() {
            private fun readResolve(): Any = UserNotFound
        }

        data object ErrorWhenUpdatingUser : ServicesExceptions() {
            private fun readResolve(): Any = ErrorWhenUpdatingUser
        }
    }

    data object Laboratories {
        data object InvalidLaboratoryId : ServicesExceptions() {
            private fun readResolve(): Any = InvalidLaboratoryId
        }

        data object LaboratoryNotFound : ServicesExceptions() {
            private fun readResolve(): Any = LaboratoryNotFound
        }

        class InvalidLaboratoryName(message: String) : ServicesExceptions(message)

        class InvalidLaboratoryDescription(message: String) : ServicesExceptions(message)

        class InvalidLaboratoryDuration(message: String) : ServicesExceptions(message)

        class InvalidLaboratoryQueueLimit(message: String) : ServicesExceptions(message)
    }

    data object Groups {
        data object InvalidGroupId : ServicesExceptions() {
            private fun readResolve(): Any = InvalidGroupId
        }

        data object GroupNotFound : ServicesExceptions() {
            private fun readResolve(): Any = GroupNotFound
        }

        data object UserAlreadyInGroup : ServicesExceptions() {
            private fun readResolve(): Any = UserAlreadyInGroup
        }

        data object UserNotInGroup : ServicesExceptions() {
            private fun readResolve(): Any = UserNotInGroup
        }

        class InvalidGroupName(message: String) : ServicesExceptions(message)

        class InvalidGroupDescription(message: String) : ServicesExceptions(message)
    }

    class InvalidQueryParam(message: String) : ServicesExceptions(message)

    class Forbidden(message: String) : ServicesExceptions(message)

    data object UnexpectedError : ServicesExceptions() {
        private fun readResolve(): Any = UnexpectedError
    }
}
