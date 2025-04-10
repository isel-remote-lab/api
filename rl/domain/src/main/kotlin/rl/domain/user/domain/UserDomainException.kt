package rl.domain.user.domain

sealed class UsersDomainException(message: String) : Exception(message) {
    data object InvalidRole : UsersDomainException("Invalid role") {
        private fun readResolve(): Any = InvalidRole
    }

    data object InvalidUsername : UsersDomainException("Invalid username") {
        private fun readResolve(): Any = InvalidUsername
    }

    data object InvalidEmail : UsersDomainException("Invalid email") {
        private fun readResolve(): Any = InvalidEmail
    }

    data object InvalidOauthId : UsersDomainException("Invalid oauthId") {
        private fun readResolve(): Any = InvalidOauthId
    }
}