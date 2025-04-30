package isel.rl.core.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import isel.rl.core.domain.Secrets
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.springframework.stereotype.Component

@Component
class JWTUtils(
    private val secrets: Secrets,
) {
    fun generateJWTToken(
        userId: String,
        oauthId: String,
        role: String,
        username: String,
        email: String,
        accessToken: String,
        issuedAt: Instant,
    ): String =
        JWT.create() // Create a JWT token
            .withIssuer(JWT_TOKEN_ISSUER)
            .withSubject(JWT_TOKEN_SUBJECT)
            .withClaim("userId", userId)
            .withClaim("oauthId", oauthId)
            .withClaim("role", role)
            .withClaim("username", username)
            .withClaim("email", email)
            .withClaim("accessToken", accessToken)
            .withIssuedAt(issuedAt.toJavaInstant())
            .sign(Algorithm.HMAC256(secrets.jwtSecret))

    fun validateJWTTokenAndRetrieveUserId(token: String): String {
        val verifier =
            JWT.require(Algorithm.HMAC256(secrets.jwtSecret))
                .withSubject(JWT_TOKEN_SUBJECT)
                .withIssuer(JWT_TOKEN_ISSUER)
                .build()

        val jwt = verifier.verify(token)
        return jwt.getClaim("userId").asString()
    }

    companion object {
        const val JWT_TOKEN_ISSUER = "Remote Lab"
        const val JWT_TOKEN_SUBJECT = "User Information"
    }
}
